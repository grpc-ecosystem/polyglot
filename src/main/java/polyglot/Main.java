package polyglot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.TextFormat;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    // Temporary scratch space for executing protoc.
    if (args[0].equals("protoc")) {
      FileDescriptorSet fileDescriptorSet = new ProtocInvoker().invoke(Paths.get(args[1]));
      logger.info("Generate file descriptor set: " + fileDescriptorSet);
      return;
    }

    CommandLineArgs arguments = CommandLineArgs.parse(args);
    String textFormatRequest = getTextProtoFromStdin();

    FileDescriptor fileDescriptor = fileDescriptorFromClass(arguments.protoClass());
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptors(fileDescriptor);
    MethodDescriptor methodDescriptor =
        serviceResolver.resolveServiceMethod(arguments.grpcMethodName());

    DynamicGrpcClient dynamicClient =
        DynamicGrpcClient.create(methodDescriptor, arguments.host(), arguments.port());
    DynamicMessage.Builder requestMessageBuilder =
        DynamicMessage.newBuilder(methodDescriptor.getInputType());
    TextFormat.getParser().merge(textFormatRequest, requestMessageBuilder);
    ListenableFuture<DynamicMessage> callFuture = dynamicClient.call(requestMessageBuilder.build());
    try {
      logger.info("Got dynamic response: " + callFuture.get());
    } catch (ExecutionException e) {
      logger.error("Failed to make rpc", e);
    }
  }

  private static FileDescriptor fileDescriptorFromClass(String className) throws Exception {
    Class<?> serviceClass = Main.class.getClassLoader().loadClass(className);
    return (FileDescriptor) serviceClass.getMethod("getDescriptor").invoke(null /* obj */);
  }

  private static String getTextProtoFromStdin() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));
    return Joiner.on("\n").join(CharStreams.readLines(reader));
  }
}
