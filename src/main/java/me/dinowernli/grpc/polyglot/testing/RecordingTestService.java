package me.dinowernli.grpc.polyglot.testing;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import polyglot.test.TestProto.TestRequest;
import polyglot.test.TestProto.TestResponse;
import polyglot.test.TestServiceGrpc.TestService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * An implementation of {@link TestService} which records the calls and produces a constant
 * response.
 */
public class RecordingTestService implements TestService {
  private final TestResponse unaryResponse;
  private final TestResponse streamResponse;
  private final List<TestRequest> recordedRequests;

  public RecordingTestService(TestResponse unaryResponse, TestResponse streamResponse) {
    this.unaryResponse = unaryResponse;
    this.streamResponse = streamResponse;
    this.recordedRequests = new ArrayList<>();
  }

  @Override
  public void testMethod(TestRequest request, StreamObserver<TestResponse> responseStream) {
    recordedRequests.add(request);
    responseStream.onNext(unaryResponse);
    responseStream.onCompleted();
  }

  public int numRequests() {
    return recordedRequests.size();
  }

  public TestRequest getRequest(int index) {
    return recordedRequests.get(index);
  }

  @Override
  public StreamObserver<TestRequest> testMethodBidi(StreamObserver<TestResponse> responseStream) {
    throw new NotImplementedException();
  }

  @Override
  public void testMethodStream(TestRequest request, StreamObserver<TestResponse> responseStream) {
    recordedRequests.add(request);
    responseStream.onNext(streamResponse);
    responseStream.onCompleted();
  }
}