package polyglot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import polyglot.HelloProto.HelloRequest;
import polyglot.HelloProto.HelloResponse;
import polyglot.HelloServiceGrpc.HelloServiceBlockingStub;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private static final int REMOTE_PORT = 12345;
  private static final String REMOTE_HOST = "localhost";

  @SuppressWarnings("unchecked")  // Casting the proto request types.
  public static void main(String[] args) throws
      IOException,
      InterruptedException,
      ClassNotFoundException,
      IllegalArgumentException,
      IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
    logger.info("Starting grpc client to [" + REMOTE_HOST + ":" + REMOTE_PORT + "]");

    // Create a reusable channel.
    Channel channel = NettyChannelBuilder.forAddress(REMOTE_HOST, REMOTE_PORT)
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();

    // Opening stubs the conventional way.
    HelloServiceBlockingStub helloStub = HelloServiceGrpc.newBlockingStub(channel);
    HelloResponse helloResponse = helloStub.sayHello(HelloRequest.newBuilder()
        .setRecipient("Polyglot")
        .build());
    logger.info("Got response: " + helloResponse);

    // ********** Scratch space below *************

    String className = "polyglot.HelloServiceGrpc";
    String serviceName = "polyglot.HelloService";
    String methodName = "SayHello";
    String generatedMethodName = "sayHello";

    // Inferred below.
    Class<? extends GeneratedMessage> requestType = null;
    Class<? extends GeneratedMessage> responseType = null;

    // Accessing descriptors.
    Class<?> helloServiceGrpcClass = Main.class.getClassLoader().loadClass(className);
    for (Field field : helloServiceGrpcClass.getDeclaredFields()) {
      if (MethodDescriptor.class.isAssignableFrom(field.getType())) {
        MethodDescriptor<?, ?> descriptor =
            (MethodDescriptor<?, ?>) field.get(null /* obj */); // Static field.
        logger.info("Found method descriptor: " + descriptor.getFullMethodName());

        ParameterizedType parametrizedType = (ParameterizedType) field.getGenericType();
        Type[] typeArguments = parametrizedType.getActualTypeArguments();

        logger.info("Request type: " + typeArguments[0].getTypeName());
        requestType = (Class<? extends GeneratedMessage>)
            Main.class.getClassLoader().loadClass(typeArguments[0].getTypeName());

        logger.info("Response type: " + typeArguments[1].getTypeName());
        responseType = (Class<? extends GeneratedMessage>)
            Main.class.getClassLoader().loadClass(typeArguments[1].getTypeName());
      }
    }

    // Parse the request type.
    GeneratedMessage.Builder<?> builder =
        (Builder<?>) requestType.getMethod("newBuilder").invoke(null /* obj */);
    String textFormatProto = "";
    TextFormat.getParser().merge(textFormatProto, builder);
    Message requestMessage = builder.build();

    // Opening stubs.
    Object reflectiveHelloStub = helloServiceGrpcClass
        .getMethod("newStub", io.grpc.Channel.class)
        .invoke(null /* obj */, channel);
    Method sayHelloMethod = reflectiveHelloStub.getClass()
        .getMethod(generatedMethodName, requestType, StreamObserver.class);
    //sayHelloMethod.invoke(reflectiveHelloStub, requestMessage);
  }
}
