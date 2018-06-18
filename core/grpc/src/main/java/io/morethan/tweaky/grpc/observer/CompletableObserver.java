package io.morethan.tweaky.grpc.observer;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.grpc.Errors;

/**
 * A {@link StreamObserver} that is waiting on completion through {@link #awaitCompletion()} which a single result or throws an occured error.
 *
 * @param <T>
 */
public class CompletableObserver<T> implements StreamObserver<T> {

    private static final Logger LOG = LoggerFactory.getLogger(CompletableObserver.class);
    private final CountDownLatch _countDownLatch = new CountDownLatch(1);
    private T _resultValue;
    private Throwable _error;
    private final String _name;

    public CompletableObserver(String name) {
        _name = name;
    }

    @Override
    public void onNext(T value) {
        _resultValue = value;
    }

    @Override
    public void onError(Throwable t) {
        if (Errors.isCancelled(t)) {
            LOG.warn(_name + " got cancelled: " + t.getMessage());
        } else {
            LOG.error(_name + " got error", t);
        }
        _error = t;
        _countDownLatch.countDown();
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
