package me.dinowernli.grpc.polyglot.testing;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polyglot.test.TestProto.TestRequest;
import polyglot.test.TestProto.TestResponse;
import polyglot.test.TestServiceGrpc.TestServiceImplBase;

/**
 * An implementation of {@link TestServiceImplBase} which records the calls and produces a constant
 * response.
 */
public class RecordingTestService extends TestServiceImplBase {
  private static final Logger logger = LoggerFactory.getLogger(RecordingTestService.class);

  /** The number of messages to wait for from client stream before ending the rpc. */
  private static final int NUM_MESSAGES_FROM_CLIENT = 3;

  /** We fake some expensive computation to shake out race conditions. */
  private static final long RESPONSE_COMPUTATION_TIME_MS = 1000L;

  private final TestResponse unaryResponse;
  private final TestResponse streamResponse;
  private final TestResponse clientStreamResponse;
  private final TestResponse bidiStreamResponse;
  private final List<TestRequest> recordedRequests;

  public RecordingTestService(
      TestResponse unaryResponse,
      TestResponse streamResponse,
      TestResponse clientStreamResponse,
      TestResponse bidiStreamResponse) {
    this.unaryResponse = unaryResponse;
    this.streamResponse = streamResponse;
    this.clientStreamResponse = clientStreamResponse;
    this.bidiStreamResponse = bidiStreamResponse;
    this.recordedRequests = new ArrayList<>();
  }

  @Override
  public void testMethod(TestRequest request, StreamObserver<TestResponse> responseStream) {
    logger.info("Handling unary method call");
    recordedRequests.add(request);
    fakeExpensiveComputation();
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
    logger.info("Handling bidi method call");
    return new StreamObserver<TestRequest>() {
      int numRequestMessages = 0;

      @Override
      public void onNext(TestRequest testRequest) {
        ++numRequestMessages;
        recordedRequests.add(testRequest);
        responseStream.onNext(bidiStreamResponse);
        if (numRequestMessages >= NUM_MESSAGES_FROM_CLIENT) {
          fakeExpensiveComputation();
          responseStream.onCompleted();
        }
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Got incoming error", t);
      }

      @Override
      public void onCompleted() {
        // Do nothing.
      }
    };
  }

  @Override
  public void testMethodStream(TestRequest request, StreamObserver<TestResponse> responseStream) {
    logger.info("Handling server streaming method call");
    recordedRequests.add(request);
    fakeExpensiveComputation();
    responseStream.onNext(streamResponse);
    responseStream.onCompleted();
  }

  @Override
  public StreamObserver<TestRequest> testMethodClientStream(
      StreamObserver<TestResponse> responseStream) {
    logger.info("Handling client streaming method call");
    return new StreamObserver<TestRequest>() {
      int numRequestMessages = 0;

      @Override
      public void onNext(TestRequest testRequest) {
        ++numRequestMessages;
        recordedRequests.add(testRequest);
        if (numRequestMessages >= NUM_MESSAGES_FROM_CLIENT) {
          fakeExpensiveComputation();
          responseStream.onNext(clientStreamResponse);
          responseStream.onCompleted();
        }
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Got incoming error", t);
      }

      @Override
      public void onCompleted() {
        // Do nothing.
      }
    };
  }

  private static void fakeExpensiveComputation() {
    try {
      Thread.sleep(RESPONSE_COMPUTATION_TIME_MS);
    } catch (InterruptedException e) {
      logger.error("Interrupted while sleeping", e);
    }
  }
}