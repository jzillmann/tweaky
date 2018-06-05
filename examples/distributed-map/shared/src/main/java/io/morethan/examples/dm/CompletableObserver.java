package io.morethan.examples.dm;

import java.util.concurrent.CountDownLatch;

import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.grpc.Errors;

/**
 * A {@link StreamObserver} that is waiting on completion through {@link #awaitCompletion()} which a single result or throws an occured error.
 *
 * @param <T>
 */
public class CompletableObserver<T> implements StreamObserver<T> {

    private final CountDownLatch _countDownLatch = new CountDownLatch(1);
    private T _resultValue;
    private Throwable _error;

    @Override
    public void onNext(T value) {
        _resultValue = value;
    }

    @Override
    public void onError(Throwable t) {
        _error = t;
    }

    @Override
    public void onCompleted() {
        _countDownLatch.countDown();
    }

    public T awaitCompletion() throws InterruptedException {
        _countDownLatch.await();
        throwOnError();
        return _resultValue;
    }

    public void throwOnError() {
        if (_error != null) {
            throw Errors.unwrapped(_error);
        }
    }

}
