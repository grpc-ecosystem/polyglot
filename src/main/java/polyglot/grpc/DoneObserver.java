package polyglot.grpc;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which notifies when the stream has finished, i.e., when it is either
 * complete or an error occurred.
 */
class DoneObserver<T> implements StreamObserver<T> {
  @Override
  public synchronized void onCompleted() {
    notify();
  }

  @Override
  public synchronized void onError(Throwable t) {
    notify();
  }

  @Override
  public void onNext(T next) {
    // Do nothing.
  }
}