package io.morethan.tweaky.grpc.observer;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} that delegates all calls to another {@link StreamObserver} instance.
 *
 * @param <T>
 */
public class ForwardingStreamObserver<T> implements StreamObserver<T> {

    private final StreamObserver<T> _delegate;

    public ForwardingStreamObserver(StreamObserver<T> delegate) {
        _delegate = delegate;
    }

    @Override
    public void onNext(T value) {
        _delegate.onNext(value);
    }

    @Override
    public void onError(Throwable t) {
        _delegate.onError(t);
    }

    @Override
    public void onCompleted() {
        _delegate.onCompleted();
    }

}
