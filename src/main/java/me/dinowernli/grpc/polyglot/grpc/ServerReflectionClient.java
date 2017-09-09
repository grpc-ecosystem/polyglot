package me.dinowernli.grpc.polyglot.grpc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.grpc.Channel;
import io.grpc.reflection.v1alpha.ListServiceResponse;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.reflection.v1alpha.ServerReflectionResponse.MessageResponseCase;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerReflectionClient {
  private static final Logger logger = LoggerFactory.getLogger(ServerReflectionClient.class);
  private static final long RPC_DEADLINE_MS = 5_000;
  private static final ServerReflectionRequest LIST_SERVICES_REQUEST =
      ServerReflectionRequest.newBuilder()
          .setListServices("")  // Note sure what this is for, appears to be ignored.
          .build();

  private final Channel channel;
  private final ExecutorService executor;

  /** Returns a new reflection client using the supplied channel. */
  public static ServerReflectionClient create(Channel channel) {
    return new ServerReflectionClient(channel, Executors.newCachedThreadPool());
  }

  private ServerReflectionClient(Channel channel, ExecutorService executor) {
    this.channel = channel;
    this.executor = executor;
  }

  /** Asks the remote server to list its services and completes when the server responds. */
  public ListenableFuture<ImmutableList<String>> listServices() {
    ResponseObserver responseStream = new ResponseObserver();
    StreamObserver<ServerReflectionRequest> requestStream = ServerReflectionGrpc.newStub(channel)
        .withDeadlineAfter(RPC_DEADLINE_MS, TimeUnit.MILLISECONDS)
        .serverReflectionInfo(responseStream);
    requestStream.onNext(LIST_SERVICES_REQUEST);

    ListenableFuture<ImmutableList<String>> result = responseStream.resultFuture();
    Futures.addCallback(result, new RpcCompletingFutureCallback<>(requestStream), executor);
    return result;
  }

  /**
   * Returns a {@link FileDescriptorSet} containing all the transitive dependencies of the supplied
   * service, as provided by the remote server.
   */
  public ListenableFuture<FileDescriptorSet> lookupService(String serviceName) {
    throw new UnsupportedOperationException();
  }

  private static class ResponseObserver implements StreamObserver<ServerReflectionResponse> {
    private final SettableFuture<ImmutableList<String>> resultFuture;

    private ResponseObserver() {
      resultFuture = SettableFuture.create();
    }

    private ListenableFuture<ImmutableList<String>> resultFuture() {
      return resultFuture;
    }

    @Override
    public void onNext(ServerReflectionResponse serverReflectionResponse) {
      MessageResponseCase responseCase = serverReflectionResponse.getMessageResponseCase();
      switch (responseCase) {
        case LIST_SERVICES_RESPONSE:
          handleListServiceRespones(serverReflectionResponse.getListServicesResponse());
          break;
        default:
          logger.warn("Got unknown reflection response type: " + responseCase);
          break;
      }
    }

    @Override
    public void onError(Throwable t) {
      logger.error("Error in the server reflection rpc", t);
      resultFuture.setException(t);
    }

    @Override
    public void onCompleted() {
      logger.error("Unexpected completion of the server reflection rpc");
      resultFuture.setException(new RuntimeException("Unexpected end of rpc"));
    }

    private void handleListServiceRespones(ListServiceResponse response) {
      ImmutableList.Builder<String> servicesBuilder = ImmutableList.builder();
      response.getServiceList().forEach(service -> servicesBuilder.add(service.getName()));
      resultFuture.set(servicesBuilder.build());
    }
  }

  private static class RpcCompletingFutureCallback<T> implements FutureCallback<T> {
    private final StreamObserver<?> requestStream;

    private RpcCompletingFutureCallback(StreamObserver<?> requestStream) {
      this.requestStream = requestStream;
    }

    @Override
    public void onSuccess(Object o) {
      requestStream.onCompleted();
    }

    @Override
    public void onFailure(Throwable throwable) {
      requestStream.onCompleted();
    }
  }
}
