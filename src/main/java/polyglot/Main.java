package polyglot;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;

import io.grpc.Channel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import polyglot.HelloProto.HelloRequest;
import polyglot.HelloProto.HelloResponse;
import polyglot.HelloServiceGrpc.HelloServiceBlockingStub;
import polyglot.HelloServiceGrpc.HelloServiceFutureStub;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private static final int REMOTE_PORT = 12345;
  private static final String REMOTE_HOST = "localhost";
  private static final HelloRequest REQUEST = HelloRequest.newBuilder()
      .setRecipient("Polyglot")
      .build();

  private static Channel newChannel() {
    return NettyChannelBuilder.forAddress(REMOTE_HOST, REMOTE_PORT)
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();
  }

  public static void main(String[] args) throws
      IOException,
      InterruptedException,
      ClassNotFoundException,
      IllegalArgumentException,
      IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
    logger.info("Starting grpc client to [" + REMOTE_HOST + ":" + REMOTE_PORT + "]");

    // Open a regular stub..
    HelloServiceBlockingStub helloStub = HelloServiceGrpc.newBlockingStub(newChannel());
    HelloResponse helloResponse = helloStub.sayHello(HelloRequest.newBuilder()
        .setRecipient("Polyglot")
        .build());
    logger.info("Got response from blocking: " + helloResponse);

    // Open a future stub.
    HelloServiceFutureStub helloFutureStub = HelloServiceGrpc.newFutureStub(newChannel());
    ListenableFuture<HelloResponse> responseFuture = helloFutureStub.sayHello(REQUEST);
    try {
      logger.info("Got response with future: " + responseFuture.get());
    } catch (ExecutionException e) {
      logger.error("Failed to make rpc", e);
    }

    // ********** Scratch space below *************

    String className = "polyglot.HelloServiceGrpc";
    String serviceName = "polyglot.HelloService";
    String methodName = "SayHello";
    String generatedMethodName = "sayHello";

    logger.info("Creating grpc client");
    Class<?> grpcClass = Main.class.getClassLoader().loadClass(className);
    GrpcClient client =
        GrpcClient.forGrpcClass(grpcClass, generatedMethodName, REMOTE_HOST, REMOTE_PORT);
    logger.info("Making generic call");
    ListenableFuture<Message> genericResponseFuture = client.call(REQUEST);
    try {
      logger.info("Got response with future: " + genericResponseFuture.get());
    } catch (ExecutionException e) {
      logger.error("Failed to make rpc", e);
    }

//    // Parse the request type.
//    GeneratedMessage.Builder<?> builder =
//        (Builder<?>) requestType.getMethod("newBuilder").invoke(null /* obj */);
//    String textFormatProto = "";
//    TextFormat.getParser().merge(textFormatProto, builder);
//    Message requestMessage = builder.build();
//
//    // Opening stubs.
//    Object reflectiveHelloStub = helloServiceGrpcClass
//        .getMethod("newStub", io.grpc.Channel.class)
//        .invoke(null /* obj */, newChannel());
//    Method sayHelloMethod = reflectiveHelloStub.getClass()
//        .getMethod(generatedMethodName, requestType, StreamObserver.class);
//    //sayHelloMethod.invoke(reflectiveHelloStub, requestMessage);
  }
}
