package me.dinowernli.grpc.polyglot.grpc;

import com.google.common.util.concurrent.FutureCallback;

import io.grpc.stub.StreamObserver;

/**
 * A {@link FutureCallback} which provides an adapter from a future to a stream with a single
 * response.
 */
class SingleResponseCallback<T> implements FutureCallback<T> {
  private final StreamObserver<T> streamObserver;

  SingleResponseCallback(StreamObserver<T> streamObserver) {
    this.streamObserver = streamObserver;
  }

  @Override
  public void onFailure(Throwable t) {
    streamObserver.onError(t);
  }

  @Override
  public void onSuccess(T result) {
    streamObserver.onNext(result);
    streamObserver.onCompleted();
  }
}