package polyglot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import polyglot.ProtocInvoker.ProtocInvocationException;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Usage: " + CommandLineArgs.getUsage());
    CommandLineArgs arguments = CommandLineArgs.parse(args);

    FileDescriptorSet fileDescriptorSet = getFileDescriptorSet(arguments.protoRoot());
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
    MethodDescriptor methodDescriptor =
        serviceResolver.resolveServiceMethod(arguments.grpcMethodName());

    DynamicGrpcClient dynamicClient =
        DynamicGrpcClient.create(methodDescriptor, arguments.host(), arguments.port());
    DynamicMessage requestMessage = getProtoFromStdin(methodDescriptor.getInputType());

    ListenableFuture<DynamicMessage> callFuture = dynamicClient.call(requestMessage);
    Optional<DynamicMessage> response = Optional.empty();
    try {
      response = Optional.of(callFuture.get());
      logger.info("Got dynamic response: " + response.get());
    } catch (ExecutionException | InterruptedException e) {
      logger.error("Failed to make rpc", e);
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

  private static FileDescriptorSet getFileDescriptorSet(Path protoRoot) {
    try {
      return new ProtocInvoker().invoke(protoRoot);
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
}
