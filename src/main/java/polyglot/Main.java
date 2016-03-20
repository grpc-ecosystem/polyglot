package polyglot;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.TextFormat;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private static final int REMOTE_PORT = 12345;
  private static final String REMOTE_HOST = "localhost";

  public static void main(String[] args) throws Exception {
    String textFormatRequest = "recipient: \"Polyglot\"";
    String protoFileClass = "polyglot.HelloProto";
    String protoServiceName = "HelloService";
    String protoMethodName = "SayHello";

    FileDescriptor fileDescriptor = fileDescriptorFromClass(protoFileClass);
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptors(fileDescriptor);
    MethodDescriptor methodDescriptor =
        serviceResolver.resolveServiceMethod(protoServiceName, protoMethodName);

    DynamicGrpcClient dynamicClient =
        DynamicGrpcClient.create(methodDescriptor, REMOTE_HOST, REMOTE_PORT);
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
}
