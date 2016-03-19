package polyglot;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import polyglot.HelloProto.HelloRequest;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private static final int REMOTE_PORT = 12345;
  private static final String REMOTE_HOST = "localhost";

  public static void main(String[] args) throws Exception {
    String className = "polyglot.HelloServiceGrpc";
    String serviceName = "polyglot.HelloService";
    String methodName = "SayHello";
    String generatedMethodName = "sayHello";
    String textFormatRequest = "recipient: \"Polyglot\"";

    testStringResolution("foo.Test", "BazService");
    testServiceResolution(HelloProto.getDescriptor().toProto(), "Hello");

    // Parse the request type.
    Class<? extends GeneratedMessage> requestType = HelloRequest.class;
    GeneratedMessage.Builder<?> builder =
        (GeneratedMessage.Builder<?>) requestType.getMethod("newBuilder").invoke(null /* obj */);
    TextFormat.getParser().merge(textFormatRequest, builder);
    Message requestMessage = builder.build();

    Class<?> grpcClass = Main.class.getClassLoader().loadClass(className);
    logger.info("Creating grpc client");
    GrpcClient client =
        GrpcClient.forGrpcClass(grpcClass, generatedMethodName, REMOTE_HOST, REMOTE_PORT);
    logger.info("Making generic call");
    ListenableFuture<Message> genericResponseFuture = client.call(requestMessage);
    try {
      logger.info("Got response with future: " + genericResponseFuture.get());
    } catch (ExecutionException e) {
      logger.error("Failed to make rpc", e);
    }
  }

  static void testStringResolution(String javaOuterClass, String serviceName) throws Exception {
    Class<?> outerClass = Main.class.getClassLoader().loadClass(javaOuterClass);
    FileDescriptor fileDescriptor =
        (FileDescriptor) outerClass.getMethod("getDescriptor").invoke(null /* obj */);
    testServiceResolution(fileDescriptor.toProto(), serviceName);
  }

  static void testServiceResolution(FileDescriptorProto fileDescriptorProto, String serviceName) {
    logger.info("Service name: " + serviceName);
    logger.info("File descriptor proto: \n" + fileDescriptorProto);
  }
}
