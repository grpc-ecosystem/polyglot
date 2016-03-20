package polyglot.server;

import io.grpc.stub.StreamObserver;
import polyglot.HelloProto.HelloRequest;
import polyglot.HelloProto.HelloResponse;

public class HelloServiceImpl implements HelloService {
  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseStream) {
    responseStream.onNext(HelloResponse.newBuilder()
        .setMessage("Hello, " + request.getRecipient())
        .build());
    responseStream.onCompleted();
  }
}
