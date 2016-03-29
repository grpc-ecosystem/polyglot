package polyglot;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCalls;
import io.netty.handler.ssl.SslContext;

/** A grpc client which operates on dynamic messages. */
public class DynamicGrpcClient {
  private static final Logger logger = LoggerFactory.getLogger(DynamicGrpcClient.class);
  private final MethodDescriptor protoMethodDescriptor;
  private final Channel channel;

  /** Creates a client for the supplied method, talking to the supplied endpoint. */
  public static DynamicGrpcClient create(
      MethodDescriptor protoMethod, HostAndPort endpoint, boolean useTls) {
    Channel channel = useTls ? createTlsChannel(endpoint) : createPlaintextChannel(endpoint);
    return new DynamicGrpcClient(protoMethod, channel);
  }

  private DynamicGrpcClient(MethodDescriptor protoMethodDescriptor, Channel channel) {
    this.protoMethodDescriptor = protoMethodDescriptor;
    this.channel = channel;
    logger.info("Created client for method: " + getFullMethodName());
  }

  /** Makes an rpc to the remote endpoint and returns the remote response. */
  public ListenableFuture<DynamicMessage> call(DynamicMessage request) {
    return ClientCalls.futureUnaryCall(
        channel.newCall(createGrpcMethodDescriptor(), CallOptions.DEFAULT),
        request);
  }

  private String getFullMethodName() {
    String serviceName = protoMethodDescriptor.getService().getFullName();
    String methodName = protoMethodDescriptor.getName();
    return io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName);
  }

  private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor() {
    return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>create(
        MethodType.UNARY,
        getFullMethodName(),
        new DynamicMessageMarshaller(protoMethodDescriptor.getInputType()),
        new DynamicMessageMarshaller(protoMethodDescriptor.getOutputType()));
  }

  private static Channel createPlaintextChannel(HostAndPort endpoint) {
    return NettyChannelBuilder.forAddress(endpoint.getHostText(), endpoint.getPort())
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();
  }

  private static Channel createTlsChannel(HostAndPort endpoint) {
    SslContext sslContext;
    try {
      sslContext = GrpcSslContexts.forClient().build();
    } catch (SSLException e) {
      throw new RuntimeException("Failed to create ssl context", e);
    }

    return NettyChannelBuilder.forAddress(endpoint.getHostText(), endpoint.getPort())
        .sslContext(sslContext)
        .negotiationType(NegotiationType.TLS)
        .build();
  }
}
