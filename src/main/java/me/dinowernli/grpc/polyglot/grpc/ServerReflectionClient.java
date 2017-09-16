package me.dinowernli.grpc.polyglot.grpc;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.InvalidProtocolBufferException;
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
  private static final long LIST_RPC_DEADLINE_MS = 1_000;
  private static final long LOOKUP_RPC_DEADLINE_MS = 10_000;
  private static final ServerReflectionRequest LIST_SERVICES_REQUEST =
      ServerReflectionRequest.newBuilder()
          .setListServices("")  // Note sure what this is for, appears to be ignored.
          .build();

  private final Channel channel;

  /** Returns a new reflection client using the supplied channel. */
  public static ServerReflectionClient create(Channel channel) {
    return new ServerReflectionClient(channel);
  }

  private ServerReflectionClient(Channel channel) {
    this.channel = channel;
  }

  /** Asks the remote server to list its services and completes when the server responds. */
  public ListenableFuture<ImmutableList<String>> listServices() {
    ListServicesHandler rpcHandler = new ListServicesHandler();
    StreamObserver<ServerReflectionRequest> requestStream = ServerReflectionGrpc.newStub(channel)
        .withDeadlineAfter(LIST_RPC_DEADLINE_MS, TimeUnit.MILLISECONDS)
        .serverReflectionInfo(rpcHandler);
    return rpcHandler.start(requestStream);
  }

  /**
   * Returns a {@link FileDescriptorSet} containing all the transitive dependencies of the supplied
   * service, as provided by the remote server.
   */
  public ListenableFuture<FileDescriptorSet> lookupService(String serviceName) {
    LookupServiceHandler rpcHandler = new LookupServiceHandler(serviceName);
    StreamObserver<ServerReflectionRequest> requestStream = ServerReflectionGrpc.newStub(channel)
        .withDeadlineAfter(LOOKUP_RPC_DEADLINE_MS, TimeUnit.MILLISECONDS)
        .serverReflectionInfo(rpcHandler);
    return rpcHandler.start(requestStream);
  }

  /** Handles the rpc life cycle of a single list operation. */
  private static class ListServicesHandler implements StreamObserver<ServerReflectionResponse> {
    private final SettableFuture<ImmutableList<String>> resultFuture;
    private StreamObserver<ServerReflectionRequest> requestStream;

    private ListServicesHandler() {
      resultFuture = SettableFuture.create();
    }

    ListenableFuture<ImmutableList<String>> start(
        StreamObserver<ServerReflectionRequest> requestStream) {
      this.requestStream = requestStream;
      requestStream.onNext(LIST_SERVICES_REQUEST);
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
      if (!resultFuture.isDone()) {
        logger.error("Unexpected completion of the server reflection rpc");
        resultFuture.setException(new RuntimeException("Unexpected end of rpc"));
      }
    }

    private void handleListServiceRespones(ListServiceResponse response) {
      ImmutableList.Builder<String> servicesBuilder = ImmutableList.builder();
      response.getServiceList().forEach(service -> servicesBuilder.add(service.getName()));
      resultFuture.set(servicesBuilder.build());
      requestStream.onCompleted();
    }
  }

  /** Handles the rpc life cycle of a single lookup operation. */
  private static class LookupServiceHandler implements StreamObserver<ServerReflectionResponse> {
    private final SettableFuture<FileDescriptorSet> resultFuture;
    private final String serviceName;
    private StreamObserver<ServerReflectionRequest> requestStream;
    private HashMap<String, FileDescriptorProto> fileDescriptors;

    // Used to notice when we've received all the files we've asked for and we can end the rpc.
    private int outstandingRequests;

    private LookupServiceHandler(String serviceName) {
      this.serviceName = serviceName;
      this.resultFuture = SettableFuture.create();
      this.fileDescriptors = new HashMap<>();
      this.outstandingRequests = 0;
    }

    ListenableFuture<FileDescriptorSet> start(
        StreamObserver<ServerReflectionRequest> requestStream) {
      this.requestStream = requestStream;
      requestStream.onNext(requestForSymbol(serviceName));
      ++outstandingRequests;
      return resultFuture;
    }

    @Override
    public void onNext(ServerReflectionResponse response) {
      MessageResponseCase responseCase = response.getMessageResponseCase();
      switch (responseCase) {
        case FILE_DESCRIPTOR_RESPONSE:
          response.getFileDescriptorResponse().getFileDescriptorProtoList()
              .forEach(this::handleFileDescriptor);
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
      if (!resultFuture.isDone()) {
        logger.error("Unexpected completion of the server reflection rpc");
        resultFuture.setException(new RuntimeException("Unexpected end of rpc"));
      }
    }

    private void handleFileDescriptor(ByteString fileDescriptorBytes) {
      FileDescriptorProto fileDescriptor;
      try {
        fileDescriptor = FileDescriptorProto.newBuilder()
            .mergeFrom(fileDescriptorBytes)
            .build();
      } catch (InvalidProtocolBufferException e) {
        logger.warn("Failed to parse bytes as file descriptor proto");
        return;
      }

      logger.debug("Retrieved file descriptor: " + fileDescriptor.getName());
      fileDescriptors.put(fileDescriptor.getName(), fileDescriptor);
      fileDescriptor.getDependencyList().forEach(dep -> {
        if (!fileDescriptors.containsKey(dep)) {
          requestStream.onNext(requestForDescriptor(dep));
          ++outstandingRequests;
        }
      });

      --outstandingRequests;
      if (outstandingRequests == 0) {
        logger.debug("Retrieved service definition for [{}] by reflection", serviceName);
        resultFuture.set(FileDescriptorSet.newBuilder()
            .addAllFile(fileDescriptors.values())
            .build());
        requestStream.onCompleted();
      }
    }

    private static ServerReflectionRequest requestForDescriptor(String name) {
      return ServerReflectionRequest.newBuilder()
          .setFileByFilename(name)
          .build();
    }

    private static ServerReflectionRequest requestForSymbol(String symbol) {
      return ServerReflectionRequest.newBuilder()
          .setFileContainingSymbol(symbol)
          .build();
    }
  }
}
