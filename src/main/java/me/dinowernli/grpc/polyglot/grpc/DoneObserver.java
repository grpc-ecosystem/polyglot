package me.dinowernli.grpc.polyglot.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} holding a future which completes when the rpc terminates.
 */
class DoneObserver<T> implements StreamObserver<T> {
  private final SettableFuture<Void> doneFuture;

  DoneObserver() {
    this.doneFuture = SettableFuture.create();
  }

  @Override
  public synchronized void onCompleted() {
    doneFuture.set(null);
  }

  @Override
  public synchronized void onError(Throwable t) {
    doneFuture.set(null);
  }

  @Override
  public void onNext(T next) {
    // Do nothing.
  }

  /**
   * Returns a future which completes when the rpc finishes. Note that even if the rpc finishes
   * with an error, the returned future always completes successfully.
   */
  ListenableFuture<Void> getCompletionFuture() {
    return doneFuture;
  }
}