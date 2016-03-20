package polyglot;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
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

    // Fetch the service and the method descriptor.
    Class<?> serviceClass = Main.class.getClassLoader().loadClass(protoFileClass);
    FileDescriptor fileDescriptor =
        (FileDescriptor) serviceClass.getMethod("getDescriptor").invoke(null /* obj */);
    ServiceDescriptor protobufService = fileDescriptor.findServiceByName(protoServiceName);
    if (protobufService == null) {
      logger.error("Service is null");
    }

    MethodDescriptor protobufMethod = protobufService.findMethodByName(protoMethodName);
    if (protobufMethod == null) {
      logger.error("Protobufmethod is null");
      for (MethodDescriptor methodDescriptor : protobufService.getMethods()) {
        logger.error(">>>>> available: " + methodDescriptor.getName());
        logger.error(">>>>> available: " + methodDescriptor.getFullName());
      }
    }

    DynamicGrpcClient dynamicClient = DynamicGrpcClient.create(protobufMethod, REMOTE_HOST, REMOTE_PORT);
    DynamicMessage.Builder requestMessageBuilder = DynamicMessage.newBuilder(protobufMethod.getInputType());
    TextFormat.getParser().merge(textFormatRequest, requestMessageBuilder);
    ListenableFuture<DynamicMessage> callFuture = dynamicClient.call(requestMessageBuilder.build());
    try {
      logger.info("Got dynamic response: " + callFuture.get());
    } catch (ExecutionException e) {
      logger.error("Failed to make rpc", e);
    }
  }
}
