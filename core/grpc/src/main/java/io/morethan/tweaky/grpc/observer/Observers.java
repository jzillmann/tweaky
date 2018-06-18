package io.morethan.tweaky.grpc.observer;

import java.util.function.Function;

import io.grpc.stub.AbstractStub;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

public class Observers {

    public static <StubT extends AbstractStub<StubT>, ReqT, ResT> StreamObserver<ReqT> blockingRequestStream(
            StreamObserver<ResT> responseObserver,
            Function<StreamObserver<ResT>, StreamObserver<ReqT>> requestStreamSupplier) {

        final Object lock = new Object();
        OnReadyHandlerInstallingClientResponseObserver<ReqT, ResT> blockingObserver = new OnReadyHandlerInstallingClientResponseObserver<>(responseObserver, () -> {
            synchronized (lock) {
                lock.notifyAll();
            }
        });

        CallStreamObserver<ReqT> requestObserver = (CallStreamObserver<ReqT>) requestStreamSupplier.apply(blockingObserver);
        return new FlowControlledRequestObserver<>(lock, requestObserver);
    }

    private static class OnReadyHandlerInstallingClientResponseObserver<ReqT, ResT> extends ForwardingStreamObserver<ResT> implements ClientResponseObserver<ReqT, ResT> {

        private final Runnable _onReadyHandler;

        public OnReadyHandlerInstallingClientResponseObserver(StreamObserver<ResT> delegate, Runnable onReadyHandler) {
            super(delegate);
            _onReadyHandler = onReadyHandler;
        }

        @Override
        public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
            requestStream.disableAutoInboundFlowControl();
            requestStream.setOnReadyHandler(_onReadyHandler);
        }

    }

    private static class FlowControlledRequestObserver<ReqT> implements StreamObserver<ReqT> {

        private final CallStreamObserver<ReqT> _requestStream;
        private final Object _lock;

        public FlowControlledRequestObserver(Object lock, CallStreamObserver<ReqT> requestStream) {
            _lock = lock;
            _requestStream = requestStream;
        }

        @Override
        public void onNext(ReqT value) {
            while (!_requestStream.isReady()) {
                synchronized (_lock) {
                    if (!_requestStream.isReady()) {
                        try {
                            _lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                // TODO benchmark other blocking strategies
                // Thread.yield();
                // try {
                // Thread.sleep(10);
                // } catch (InterruptedException e) {
                // Thread.currentThread().interrupt();
                // }
            }
            // System.out.println("Inserter.put(): " + value);
            _requestStream.onNext(value);
        }

        @Override
        public void onError(Throwable t) {
            _requestStream.onError(t);

        }

        @Override
        public void onCompleted() {
            _requestStream.onCompleted();
        }

    }
}
