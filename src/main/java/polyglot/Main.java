package polyglot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

import io.grpc.stub.StreamObserver;
import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.OauthConfiguration;
import polyglot.ConfigProto.OauthConfiguration.AccessTokenCredentials;
import polyglot.ConfigProto.OauthConfiguration.CredentialsCase;
import polyglot.ConfigProto.OauthConfiguration.OauthClient;
import polyglot.config.CommandLineArgs;
import polyglot.config.ConfigurationLoader;
import polyglot.grpc.DynamicGrpcClient;
import polyglot.oauth2.RefreshTokenCredentials;
import polyglot.protobuf.ProtocInvoker;
import polyglot.protobuf.ProtocInvoker.ProtocInvocationException;
import polyglot.protobuf.ServiceResolver;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    // Fix the logging setup.
    disableStdout();
    setupJavaUtilLogging();

    final CommandLineArgs arguments;
    try {
      arguments = CommandLineArgs.parse(args);
    } catch (RuntimeException e) {
      logger.info("Usage: polyglot " + CommandLineArgs.getUsage());
      return;
    }

    ConfigurationLoader configLoader = arguments.configSetPath().isPresent()
        ? ConfigurationLoader.forFile(arguments.configSetPath().get())
        : ConfigurationLoader.forDefaultConfigSet();
    Configuration config = arguments.configName().isPresent()
        ? configLoader.getNamedConfiguration(arguments.configName().get())
        : configLoader.getDefaultConfiguration();
    logger.info("Loaded configuration: " + config.getName());

    logger.info("Loading proto file descriptors");
    FileDescriptorSet fileDescriptorSet =
        getFileDescriptorSet(arguments.protoRoot(), arguments.protocProtoPath());
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
    MethodDescriptor methodDescriptor =
        serviceResolver.resolveServiceMethod(arguments.grpcMethodName());

    logger.info("Creating dynamic grpc client");
    DynamicGrpcClient dynamicClient;
    if (config.getCallConfig().hasOauthConfig()) {
      OauthConfiguration oauthConfig = config.getCallConfig().getOauthConfig();

      Credentials credentials;
      if (oauthConfig.getCredentialsCase() == CredentialsCase.ACCESS_TOKEN_CREDENTIALS) {
        AccessTokenCredentials accessTokenCreds = oauthConfig.getAccessTokenCredentials();
        AccessToken accessToken = new AccessToken(
            readFile(Paths.get(accessTokenCreds.getAccessTokenPath())), null);

        logger.info("Using access token credentials");
        credentials = new OAuth2Credentials(accessToken);
      } else if (oauthConfig.getCredentialsCase() == CredentialsCase.REFRESH_TOKEN_CREDENTIALS) {
        String exchangeUrl = oauthConfig.getRefreshTokenCredentials().getTokenEndpointUrl();
        String refreshToken = readFile(
            Paths.get(oauthConfig.getRefreshTokenCredentials().getRefreshTokenPath()));
        OauthClient oauthClient = oauthConfig.getRefreshTokenCredentials().getClient();

        logger.info("Using refresh token credentials");
        credentials = RefreshTokenCredentials.create(oauthClient, refreshToken, exchangeUrl);
      } else {
        throw new IllegalArgumentException(
            "Unknown oauth crdentials: " + oauthConfig.getCredentialsCase());
      }
      dynamicClient = DynamicGrpcClient.createWithCredentials(
          methodDescriptor, arguments.endpoint(), config.getCallConfig().getUseTls(), credentials);
    } else {
      dynamicClient = DynamicGrpcClient.create(
          methodDescriptor, arguments.endpoint(), config.getCallConfig().getUseTls());
    }


    DynamicMessage requestMessage = getProtoFromStdin(methodDescriptor.getInputType());

    logger.info("Making rpc call to endpoint: " + arguments.endpoint());
    ImmutableList.Builder<DynamicMessage> responsesBuilder = ImmutableList.builder();

    StreamObserver<DynamicMessage> streamObserver = new StreamObserver<DynamicMessage>() {
      @Override
      public void onNext(DynamicMessage response) {
        logger.info("Got rpc response: " + response);
        responsesBuilder.add(response);
      }

      @Override
      public void onError(Throwable t) {
        logger.info("Got rpc error: ", t);
      }

      @Override
      public void onCompleted() {
        logger.info("Rpc completed successfully");
      }
    };

    try {
      dynamicClient.call(requestMessage, streamObserver).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Caught exeception while waiting for rpc", e);
    }

    ImmutableList<DynamicMessage> responses = responsesBuilder.build();

    if (arguments.outputPath().isPresent()) {
      if (responses.size() != 1) {
        logger.warn(
            "Got unexpected number of responses, skipping write to file: " + responses.size());
      } else {
        writeToFile(arguments.outputPath().get(), responses.get(0).toString());
      }
    }
  }

  private static void writeToFile(Path path, String content) {
    try {
      Files.write(path, content.toString().getBytes(Charsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException("Unable to write to file: " + path.toString(), e);
    }
  }

  private static String readFile(Path path) {
    try {
      return Joiner.on('\n').join(Files.readAllLines(path));
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file: " + path.toString(), e);
    }
  }

  private static FileDescriptorSet getFileDescriptorSet(
      Path protoRoot, Optional<Path> protocProtoPath) {
    try {
      return new ProtocInvoker(protocProtoPath).invoke(protoRoot);
    } catch (ProtocInvocationException e) {
      throw new RuntimeException("Failed to invoke the protoc binary", e);
    }
  }

  private static DynamicMessage getProtoFromStdin(Descriptor protoDescriptor) {
    final String protoText;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));
      protoText = Joiner.on("\n").join(CharStreams.readLines(reader));
    } catch (IOException e) {
      throw new RuntimeException("Unable to read text proto input stream", e);
    }

    DynamicMessage.Builder resultBuilder = DynamicMessage.newBuilder(protoDescriptor);
    try {
      TextFormat.getParser().merge(protoText, resultBuilder);
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse text proto", e);
    }
    return resultBuilder.build();
  }

  /** Redirects the output of standard java loggers to our slf4j handler. */
  private static void setupJavaUtilLogging() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  /** Disables stdout altogether. Necessary because some library prints... */
  private static void disableStdout() {
    PrintStream nullPrintStream = new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        // Do nothing.
      }
    });
    System.setOut(nullPrintStream);
  }
}
