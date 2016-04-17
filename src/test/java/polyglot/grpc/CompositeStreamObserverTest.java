package polyglot.grpc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.grpc.stub.StreamObserver;


/** Unit tests for {@link CompositeStreamObserver}. */
public class CompositeStreamObserverTest {
  @Rule public MockitoRule mockitoJunitRule = MockitoJUnit.rule();
  @Mock private StreamObserver<SomeType> mockFirstObserver;
  @Mock private StreamObserver<SomeType> mockSecondObserver;

  private CompositeStreamObserver<SomeType> compositeObserver;

  @Before
  public void setUp() {
    compositeObserver = CompositeStreamObserver.of(mockFirstObserver, mockSecondObserver);
  }

  @Test
  public void callsOnNext() {
    SomeType value = new SomeType();
    compositeObserver.onNext(value);
    verify(mockFirstObserver).onNext(value);
    verify(mockSecondObserver).onNext(value);
  }

  @Test
  public void callOthersEvenIfOneErrorsOut() {
    SomeType value = new SomeType();
    doThrow(new RuntimeException()).when(mockFirstObserver).onNext(value);
    compositeObserver.onNext(value);
    verify(mockFirstObserver).onNext(value);
    verify(mockSecondObserver).onNext(value);
  }

  @Test
  public void callsOnComplete() {
    compositeObserver.onCompleted();
    verify(mockFirstObserver).onCompleted();
    verify(mockSecondObserver).onCompleted();
  }

  @Test
  public void forwardsErrors() {
    Throwable t = new Exception();
    compositeObserver.onError(t);
    verify(mockFirstObserver).onError(t);
    verify(mockSecondObserver).onError(t);
  }

  private static class SomeType {
  }
}
