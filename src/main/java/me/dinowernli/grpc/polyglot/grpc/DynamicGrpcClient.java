package me.dinowernli.grpc.polyglot.grpc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLException;

import com.google.auth.Credentials;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.auth.ClientAuthInterceptor;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import me.dinowernli.grpc.polyglot.protobuf.DynamicMessageMarshaller;
import polyglot.ConfigProto.CallConfiguration;

/** A grpc client which operates on dynamic messages. */
public class DynamicGrpcClient {
  private final MethodDescriptor protoMethodDescriptor;
  private final Channel channel;
  private final ListeningExecutorService executor;

  /** Creates a client for the supplied method, talking to the supplied endpoint. */
  public static DynamicGrpcClient create(
      MethodDescriptor protoMethod,
      HostAndPort endpoint,
      CallConfiguration callConfiguration) {
    Channel channel = createChannel(endpoint, callConfiguration);
    return new DynamicGrpcClient(protoMethod, channel, createExecutorService());
  }

  /**
   * Creates a client for the supplied method, talking to the supplied endpoint. Passes the
   * supplied credentials on every rpc call.
   */
  public static DynamicGrpcClient createWithCredentials(
      MethodDescriptor protoMethod,
      HostAndPort endpoint,
      CallConfiguration callConfiguration,
      Credentials credentials) {
    ListeningExecutorService executor = createExecutorService();
    Channel channel = createChannel(endpoint, callConfiguration);
    return new DynamicGrpcClient(
        protoMethod,
        ClientInterceptors.intercept(channel, new ClientAuthInterceptor(credentials, executor)),
        executor);
  }

  @VisibleForTesting
  DynamicGrpcClient(
      MethodDescriptor protoMethodDescriptor, Channel channel, ListeningExecutorService executor) {
    this.protoMethodDescriptor = protoMethodDescriptor;
    this.channel = channel;
    this.executor = executor;
  }

  /**
   * Makes an rpc to the remote endpoint and respects the supplied callback. Returns a future which
   * terminates once the call has ended.
   */
  public ListenableFuture<Void> call(
      DynamicMessage request, StreamObserver<DynamicMessage> streamObserver) {
    return call(request, streamObserver, CallOptions.DEFAULT);
  }

  /**
   * Makes an rpc to the remote endpoint and respects the supplied callback. Returns a future which
   * terminates once the call has ended.
   */
  public ListenableFuture<Void> call(
      DynamicMessage request,
      StreamObserver<DynamicMessage> streamObserver,
      CallOptions callOptions) {
    MethodType methodType = getMethodType();
    if (methodType == MethodType.UNARY) {
      return callUnary(request, streamObserver, callOptions);
    } else {
      return callServerStreaming(request, streamObserver, callOptions);
    }
  }

  private ListenableFuture<Void> callServerStreaming(
      DynamicMessage request,
      StreamObserver<DynamicMessage> streamObserver,
      CallOptions callOptions) {
    DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
    ClientCalls.asyncServerStreamingCall(
        channel.newCall(createGrpcMethodDescriptor(), callOptions),
        request,
        CompositeStreamObserver.of(streamObserver, doneObserver));
    return submitWaitTask(doneObserver);
  }

  private ListenableFuture<Void> callUnary(
      DynamicMessage request,
      StreamObserver<DynamicMessage> streamObserver,
      CallOptions callOptions) {
    ListenableFuture<DynamicMessage> response = ClientCalls.futureUnaryCall(
        channel.newCall(createGrpcMethodDescriptor(), callOptions),
        request);

    DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
    UnaryStreamCallback<DynamicMessage> callback = new UnaryStreamCallback<>(
        CompositeStreamObserver.of(streamObserver, doneObserver));
    Futures.addCallback(response, callback);
    return submitWaitTask(doneObserver);
  }

  /** Returns a {@ListenableFuture} which completes when the supplied observer is done. */
  private ListenableFuture<Void> submitWaitTask(DoneObserver<?> doneObserver) {
    return executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        synchronized (doneObserver) {
          doneObserver.wait();
        }
        return null;
      }
    });
  }

  private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor() {
    return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>create(
        getMethodType(),
        getFullMethodName(),
        new DynamicMessageMarshaller(protoMethodDescriptor.getInputType()),
        new DynamicMessageMarshaller(protoMethodDescriptor.getOutputType()));
  }

  private String getFullMethodName() {
    String serviceName = protoMethodDescriptor.getService().getFullName();
    String methodName = protoMethodDescriptor.getName();
    return io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName);
  }

  /** Returns the appropriate method type based on whether the client or server expect streams. */
  private MethodType getMethodType() {
    boolean clientStreaming = protoMethodDescriptor.toProto().getClientStreaming();
    if (clientStreaming) {
      throw new UnsupportedOperationException("Requests with streaming clients not yet supported");
    }

    boolean serverStreaming = protoMethodDescriptor.toProto().getServerStreaming();
    if (serverStreaming) {
      return MethodType.SERVER_STREAMING;
    } else {
      return MethodType.UNARY;
    }
  }

  /**
   * Returns an executor in daemon-mode, allowing callers to actively decide whether they want to
   * wait for rpc streams to complete.
   */
  private static ListeningExecutorService createExecutorService() {
    return MoreExecutors.listeningDecorator(
        Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build()));
  }

  private static Channel createPlaintextChannel(HostAndPort endpoint) {
    return NettyChannelBuilder.forAddress(endpoint.getHostText(), endpoint.getPort())
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();
  }

  private static Channel createChannel(HostAndPort endpoint, CallConfiguration callConfiguration) {
    if (!callConfiguration.getUseTls()) {
      return createPlaintextChannel(endpoint);
    }
    return NettyChannelBuilder.forAddress(endpoint.getHostText(), endpoint.getPort())
        .sslContext(createSslContext(callConfiguration))
        .negotiationType(NegotiationType.TLS)
        .build();
  }

  private static SslContext createSslContext(CallConfiguration callConfiguration) {
    SslContextBuilder resultBuilder = GrpcSslContexts.forClient();
    if (!callConfiguration.getTlsCaCertPath().isEmpty()) {
      resultBuilder.trustManager(loadFile(callConfiguration.getTlsCaCertPath()));
    }
    try {
      return resultBuilder.build();
    } catch (SSLException e) {
      throw new RuntimeException("Unable to build sslcontext for client call", e);
    }
  }

  private static File loadFile(String fileName) {
    Path filePath = Paths.get(fileName);
    Preconditions.checkArgument(Files.exists(filePath), "File " + fileName + " was not found");
    return filePath.toFile();
  }
}
