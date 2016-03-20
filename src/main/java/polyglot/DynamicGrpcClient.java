package polyglot;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCalls;

/** A grpc client which operates on dynamic messages. */
public class DynamicGrpcClient {
  private final MethodDescriptor protoMethodDescriptor;
  private final Channel channel;

  public static DynamicGrpcClient create(MethodDescriptor protoMethod, String host, int port) {
    Channel channel = NettyChannelBuilder.forAddress(host, port)
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();
    return new DynamicGrpcClient(protoMethod, channel);
  }

  public DynamicGrpcClient(MethodDescriptor protoMethodDescriptor, Channel channel) {
    this.protoMethodDescriptor = protoMethodDescriptor;
    this.channel = channel;
  }

  public ListenableFuture<DynamicMessage> call(DynamicMessage request) {
    return ClientCalls.futureUnaryCall(
        channel.newCall(grpcMethodDescriptor(), CallOptions.DEFAULT),
        request);
  }

  private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor() {
    String serviceName = protoMethodDescriptor.getService().getFullName();
    String methodName = protoMethodDescriptor.getName();
    return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>create(
        MethodType.UNARY,
        io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName),
        new DynamicMessageMarshaller(protoMethodDescriptor.getInputType()),
        new DynamicMessageMarshaller(protoMethodDescriptor.getOutputType()));
  }
}
