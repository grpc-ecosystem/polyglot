package polyglot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

public class GrpcClient {
  private final Channel channel;
  private final String methodName;

  /** Holds the method of the grpc service used to create a stub. */
  private final Method newFutureStubMethod;

  /** Holds the request type of the call. */
  private final Class<? extends GeneratedMessage> requestType;

  public static GrpcClient forGrpcClass(
      Class<?> grpcClass, String methodName, String host, int port) {
    Channel channel = NettyChannelBuilder.forAddress(host, port)
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();
    return new GrpcClient(channel, grpcClass, methodName);
  }

  private GrpcClient(Channel channel, Class<?> grpcClass, String methodName) {
    this.methodName = methodName;
    this.channel = channel;

    try {
      this.newFutureStubMethod = grpcClass.getMethod("newFutureStub", io.grpc.Channel.class);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new IllegalArgumentException("Could not extract newFutureStub method from class", e);
    }

    Field methodDescriptorField = extractMethodDescriptorField(grpcClass, methodName);
    this.requestType = extractRequestType(methodDescriptorField);
  }

  @SuppressWarnings("unchecked")  // For casting the return value.
  public ListenableFuture<Message> call(Message request) {
    final Object futureStub;
    try {
      futureStub = newFutureStubMethod.invoke(null /* obj */, channel);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new GrpcClientException("Unable to create rpc stub", e);
    }

    final Method requestMethod;
    try {
      requestMethod = futureStub.getClass().getMethod(methodName, requestType);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new GrpcClientException("Unable to get request method", e);
    }

    final Object responseFuture;
    try {
      responseFuture = requestMethod.invoke(futureStub, request);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new GrpcClientException("Unable to invoke request method", e);
    }

    return (ListenableFuture<Message>) responseFuture;
  }

  @SuppressWarnings("unchecked")  // For casting the type of the request class.
  private static Class<? extends GeneratedMessage> extractRequestType(Field methodDescriptorField) {
    // TODO(dino): Revisit which classloader exactly to use.
    ClassLoader classLoader = GrpcClient.class.getClassLoader();

    ParameterizedType parametrizedType = (ParameterizedType) methodDescriptorField.getGenericType();
    String typeName = parametrizedType.getActualTypeArguments()[0].getTypeName();
    try {
      return (Class<? extends GeneratedMessage>) classLoader.loadClass(typeName);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Could not load request class " + typeName);
    }
  }

  /** Returns the field of grpcClass corresponding to the supplied grpc method. */
  private static Field extractMethodDescriptorField(Class<?> grpcClass, String methodName) {
    ImmutableList.Builder<String> availableDescriptors = ImmutableList.builder();
    for (Field field : grpcClass.getDeclaredFields()) {
      if (MethodDescriptor.class.isAssignableFrom(field.getType())) {
        final MethodDescriptor<?, ?> descriptor;
        try {
          descriptor = (MethodDescriptor<?, ?>) field.get(null /* obj */);
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new IllegalArgumentException("Unable to get field value for descriptor", e);
        }

        availableDescriptors.add(descriptor.getFullMethodName());
        // TODO(dino): Make this check much more robust.
        if (descriptor.getFullMethodName().toLowerCase().contains(methodName.toLowerCase())) {
          return field;
        }
      }
    }
    throw new IllegalArgumentException("Unable to find descriptor for " + methodName + " in "
        + grpcClass.getName() + ", available descriptors: " + availableDescriptors.build());
  }
}
