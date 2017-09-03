package me.dinowernli.grpc.polyglot.grpc;

import java.util.HashSet;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
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
  private static final ServerReflectionRequest LIST_SERVICES_REQUEST =
      ServerReflectionRequest.newBuilder()
          .setListServices("")  // Note sure what this is for, appears to be ignored.
          .build();

  private final StreamObserver<ServerReflectionRequest> requestStream;
  private final ResponseObserver responseStream;

  /** Returns a new reflection client using the supplied channel. */
  public static ServerReflectionClient create(Channel channel) {
    ResponseObserver responseStream = new ResponseObserver();
    StreamObserver<ServerReflectionRequest> requestStream =
        ServerReflectionGrpc.newStub(channel).serverReflectionInfo(responseStream);
    return new ServerReflectionClient(requestStream, responseStream);
  }

  private ServerReflectionClient(
      StreamObserver<ServerReflectionRequest> requestStream,
      ResponseObserver responseStream) {
    this.requestStream = requestStream;
    this.responseStream = responseStream;
  }

  /** Asks the remote server to list its services and completes when the server responds. */
  public ListenableFuture<ImmutableList<String>> listServices() {
    SettableFuture<ImmutableList<String>> resultFuture = SettableFuture.create();
    responseStream.addServiceListFuture(resultFuture);
    requestStream.onNext(LIST_SERVICES_REQUEST);
    return resultFuture;
  }

  private static class ResponseObserver implements StreamObserver<ServerReflectionResponse> {
    private final Object responseLock;
    private volatile HashSet<SettableFuture<ImmutableList<String>>> listServicesFutures;

    private ResponseObserver() {
      this.responseLock = new Object();
      this.listServicesFutures = new HashSet<>();
    }

    public void addServiceListFuture(SettableFuture<ImmutableList<String>> future) {
      synchronized (responseLock) {
        listServicesFutures.add(future);
      }
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
    }

    @Override
    public void onCompleted() {
      logger.error("Unexpected completion of the server reflection rpc");
    }

    private void handleListServiceRespones(ListServiceResponse response) {
      // Construct the resulting list of services.
      ImmutableList.Builder<String> servicesBuilder = ImmutableList.builder();
      response.getServiceList().forEach(service -> servicesBuilder.add(service.getName()));
      ImmutableList<String> services = servicesBuilder.build();

      // Snapshot the list of promises and resolve them all.
      HashSet<SettableFuture<ImmutableList<String>>> promises = null;
      synchronized (responseLock) {
        promises = listServicesFutures;
        listServicesFutures = new HashSet<>();
      }
      promises.forEach(future -> future.set(services));
    }
  }
}
