package polyglot.grpc;

import com.google.common.collect.ImmutableList;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which groups multiple observers and executes them all.
 */
class CompositeStreamObserver<T> implements StreamObserver<T> {
  private final ImmutableList<StreamObserver<T>> observers;

  @SafeVarargs
  static <T> CompositeStreamObserver<T> of(StreamObserver<T>... observers) {
    return new CompositeStreamObserver<T>(ImmutableList.copyOf(observers));
  }

  private CompositeStreamObserver(ImmutableList<StreamObserver<T>> observers) {
    this.observers = observers;
  }

  @Override
  public void onCompleted() {
    for (StreamObserver<T> observer : observers) {
      observer.onCompleted();
    }
  }

  @Override
  public void onError(Throwable t) {
    for (StreamObserver<T> observer : observers) {
      observer.onError(t);
    }
  }

  @Override
  public void onNext(T value) {
    for (StreamObserver<T> observer : observers) {
      observer.onNext(value);
    }
  }
}