package polyglot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.auth.Credentials;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.stub.StreamObserver;
import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.OutputConfiguration;
import polyglot.ConfigProto.ProtoConfiguration;
import polyglot.config.CommandLineArgs;
import polyglot.config.ConfigurationLoader;
import polyglot.grpc.CompositeStreamObserver;
import polyglot.grpc.DynamicGrpcClient;
import polyglot.io.FileMessageWriter;
import polyglot.io.LoggingMessageWriter;
import polyglot.io.LoggingStatsWriter;
import polyglot.oauth2.OauthCredentialsFactory;
import polyglot.protobuf.ProtocInvoker;
import polyglot.protobuf.ProtocInvoker.ProtocInvocationException;
import polyglot.protobuf.ServiceResolver;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    // Fix the logging setup.
    setupJavaUtilLogging();

    final CommandLineArgs arguments;
    try {
      arguments = CommandLineArgs.parse(args);
    } catch (RuntimeException e) {
      logger.info("Usage: polyglot " + CommandLineArgs.getUsage(), e);
      return;
    }

    ConfigurationLoader configLoader = arguments.configSetPath().isPresent()
        ? ConfigurationLoader.forFile(arguments.configSetPath().get())
        : ConfigurationLoader.forDefaultConfigSet();
    configLoader = configLoader.withOverrides(arguments);
    Configuration config = arguments.configName().isPresent()
        ? configLoader.getNamedConfiguration(arguments.configName().get())
        : configLoader.getDefaultConfiguration();
    logger.info("Loaded configuration: " + config.getName());

    logger.info("Loading proto file descriptors");
    FileDescriptorSet fileDescriptorSet = getFileDescriptorSet(config.getProtoConfig());
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
    MethodDescriptor methodDescriptor =
        serviceResolver.resolveServiceMethod(arguments.grpcMethodName());

    logger.info("Creating dynamic grpc client");
    DynamicGrpcClient dynamicClient;
    if (config.getCallConfig().hasOauthConfig()) {
      Credentials credentials =
          new OauthCredentialsFactory(config.getCallConfig().getOauthConfig()).getCredentials();
      dynamicClient = DynamicGrpcClient.createWithCredentials(
          methodDescriptor, arguments.endpoint(), config.getCallConfig().getUseTls(), credentials);
    } else {
      dynamicClient = DynamicGrpcClient.create(
          methodDescriptor, arguments.endpoint(), config.getCallConfig().getUseTls());
    }

    logger.info("Making rpc call to endpoint: " + arguments.endpoint());
    DynamicMessage requestMessage = getProtoFromStdin(methodDescriptor.getInputType());
    StreamObserver<DynamicMessage> streamObserver = CompositeStreamObserver.of(
        new LoggingStatsWriter(), messageOutputObserver(config.getOutputConfig()));
    try {
      dynamicClient.call(requestMessage, streamObserver).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Caught exeception while waiting for rpc", e);
    }
  }

  /** Returns an observer which writes the obtained message to the specified output. */
  private static StreamObserver<DynamicMessage> messageOutputObserver(OutputConfiguration config) {
    switch (config.getDestination()) {
      case FILE:
        return FileMessageWriter.forFile(Paths.get(config.getFilePath()));
      case LOG:
        return LoggingMessageWriter.create();
      default:
        throw new IllegalArgumentException(
            "Unrecognized output destination: " + config.getDestination());
    }
  }

  /** Invokes protoc and returns a {@link FileDescriptorSet} used for discovery. */
  private static FileDescriptorSet getFileDescriptorSet(ProtoConfiguration protoConfig) {
    try {
      return ProtocInvoker.forConfig(protoConfig).invoke();
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
      JsonFormat.parser().merge(protoText, resultBuilder);
    } catch (InvalidProtocolBufferException e) {
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
}
