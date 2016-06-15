package me.dinowernli.grpc.polyglot.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import polyglot.HelloProto.HelloRequest;
import polyglot.HelloProto.HelloResponse;
import polyglot.HelloServiceGrpc.HelloService;

public class HelloServiceImpl implements HelloService {
  private static final long STREAM_SLEEP_MILLIS = 250;

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseStream) {
    responseStream.onNext(HelloResponse.newBuilder()
        .setMessage("Hello, " + request.getRecipient())
        .build());
    responseStream.onCompleted();
  }

  @Override
  public void sayHelloStream(HelloRequest request, StreamObserver<HelloResponse> responseStream) {
    for (int i = 0; i < 8; ++i) {
      responseStream.onNext(HelloResponse.newBuilder()
          .setMessage("Hello, " + request.getRecipient() + ", part " + i)
          .build());
      try {
        Thread.sleep(STREAM_SLEEP_MILLIS);
      } catch (InterruptedException e) {
        responseStream.onError(Status.ABORTED.asException());
      }
    }
    responseStream.onCompleted();
  }
}
