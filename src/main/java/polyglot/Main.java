package polyglot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import polyglot.ProtocInvoker.ProtocInvocationException;
import polyglot.oauth2.RefreshTokenCredentials;

import com.google.auth.Credentials;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

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

    logger.info("Loading proto file descriptors");
    FileDescriptorSet fileDescriptorSet = getFileDescriptorSet(arguments.protoRoot(), arguments.protocProtoPath());
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
    MethodDescriptor methodDescriptor =
        serviceResolver.resolveServiceMethod(arguments.grpcMethodName());

    logger.info("Creating dynamic grpc client");
    DynamicGrpcClient dynamicClient;
    if (arguments.oauthConfig().isPresent()) {
      Credentials credentials = new RefreshTokenCredentials(
          arguments.oauth2RefreshToken(),
          arguments.oauthConfig().get(),
          Clock.systemDefaultZone());
      dynamicClient = DynamicGrpcClient.createWithCredentials(
          methodDescriptor, arguments.endpoint(), arguments.useTls(), credentials);
    } else {
      dynamicClient =
          DynamicGrpcClient.create(methodDescriptor, arguments.endpoint(), arguments.useTls());
    }

    DynamicMessage requestMessage = getProtoFromStdin(methodDescriptor.getInputType());

    logger.info("Making rpc call to endpoint: " + arguments.endpoint());
    ListenableFuture<DynamicMessage> callFuture = dynamicClient.call(requestMessage);
    Optional<DynamicMessage> response = Optional.empty();
    try {
      response = Optional.of(callFuture.get());
      logger.info("Rpc succeeded, got response: " + response.get());
    } catch (ExecutionException | InterruptedException e) {
      logger.error("Rpc failed", e);
    }

    if (response.isPresent() && arguments.outputPath().isPresent()) {
      writeToFile(arguments.outputPath().get(), response.get().toString());
    }
  }

  private static void writeToFile(Path path, String content) {
    try {
      Files.write(path, content.toString().getBytes(Charsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException("Unable to write to file: " + path.toString(), e);
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
