package io.morethan.tweaky.grpc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import io.grpc.ManagedChannel;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.node.MapNodeComponent;
import io.morethan.tweaky.examples.dm.node.proto.MapNodeGrpc;
import io.morethan.tweaky.examples.dm.node.proto.MapNodeGrpc.MapNodeStub;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.observer.CompletableObserver;
import io.morethan.tweaky.grpc.observer.ForwardingStreamObserver;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

@Fork(value = 2)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class FlowControlBenchmark {

    @Benchmark
    public void sleep(SleepClient client) throws Exception {
        client.put();
    }

    @Benchmark
    public void yield(YieldClient client) throws Exception {
        client.put();
    }

    @Benchmark
    public void sync(SynchronizedClient client) throws Exception {
        client.put();
    }

    @Benchmark
    public void lock(LockClient client) throws Exception {
        client.put();
    }

    @State(Scope.Benchmark)
    public static abstract class AbstractClient {

        private GrpcServer _server;
        private ManagedChannel _channel;
        private StreamObserver<PutRequest> _requestStream;

        @Setup
        public void before() {
            _server = MapNodeComponent.builder()
                    .grpcServerModule(GrpcServerModule.plaintext(0))
                    .autoRegister(false)
                    .nodeRegistryHost("localhost")
                    .nodeRegistryPort(0)
                    .token("")
                    .build()
                    .server();

            _server.startAsync().awaitRunning();
            _channel = ChannelProvider.plaintext().get("localhost", _server.getPort());
            _requestStream = createRequestStream(MapNodeGrpc.newStub(_channel), new CompletableObserver<>("Response stream"));
        }

        @TearDown
        public void after() throws Exception {
            _channel.shutdown();
            _channel.awaitTermination(10, TimeUnit.SECONDS);
            _server.stopAsync().awaitTerminated();
        }

        protected abstract StreamObserver<PutRequest> createRequestStream(MapNodeStub stub, StreamObserver<PutReply> responseStream);

        public void put() {
            _requestStream.onNext(PutRequest.newBuilder().setKey("A").setValue("1").build());
        }
    }

    public static class SleepClient extends AbstractClient {

        @Override
        protected StreamObserver<PutRequest> createRequestStream(MapNodeStub stub, StreamObserver<PutReply> responseStream) {
            return new SleepingStreamObserver<>((CallStreamObserver<PutRequest>) stub.put(responseStream));
        }

    }

    public static class YieldClient extends AbstractClient {

        @Override
        protected StreamObserver<PutRequest> createRequestStream(MapNodeStub stub, StreamObserver<PutReply> responseStream) {
            return new YieldingStreamObserver<>((CallStreamObserver<PutRequest>) stub.put(responseStream));
        }

    }

    public static class SynchronizedClient extends AbstractClient {

        @Override
        protected StreamObserver<PutRequest> createRequestStream(MapNodeStub stub, StreamObserver<PutReply> responseStream) {
            Object lock = new Object();
            CallStreamObserver<PutRequest> requestStream = (CallStreamObserver<PutRequest>) stub.put(new ClientResponseObserver<PutRequest, PutReply>() {

                @Override
                public void onNext(PutReply value) {
                    responseStream.onNext(value);
                }

                @Override
                public void onError(Throwable t) {
                    responseStream.onError(t);
                }

                @Override
                public void onCompleted() {
                    responseStream.onCompleted();
                }

                @Override
                public void beforeStart(ClientCallStreamObserver<PutRequest> requestStream) {
                    requestStream.disableAutoInboundFlowControl();
                    requestStream.setOnReadyHandler(() -> {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    });
                }
            });
            return new ForwardingStreamObserver<PutRequest>(requestStream) {
                @Override
                public void onNext(PutRequest value) {
                    while (!requestStream.isReady()) {
                        synchronized (lock) {
                            if (!requestStream.isReady()) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    super.onNext(value);
                }
            };
        }
    }

    public static class LockClient extends AbstractClient {

        @Override
        protected StreamObserver<PutRequest> createRequestStream(MapNodeStub stub, StreamObserver<PutReply> responseStream) {
            ReentrantLock lock = new ReentrantLock();
            Condition isReadyCondition = lock.newCondition();
            CallStreamObserver<PutRequest> requestStream = (CallStreamObserver<PutRequest>) stub.put(new ClientResponseObserver<PutRequest, PutReply>() {

                @Override
                public void onNext(PutReply value) {
                    responseStream.onNext(value);
                }

                @Override
                public void onError(Throwable t) {
                    responseStream.onError(t);
                }

                @Override
                public void onCompleted() {
                    responseStream.onCompleted();
                }

                @Override
                public void beforeStart(ClientCallStreamObserver<PutRequest> requestStream) {
                    requestStream.disableAutoInboundFlowControl();
                    requestStream.setOnReadyHandler(() -> {
                        lock.lock();
                        try {
                            isReadyCondition.signalAll();
                        } finally {
                            lock.unlock();
                        }
                    });
                }
            });
            return new ForwardingStreamObserver<PutRequest>(requestStream) {
                @Override
                public void onNext(PutRequest value) {
                    while (!requestStream.isReady()) {
                        lock.lock();
                        try {
                            if (!requestStream.isReady()) {
                                isReadyCondition.await();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } finally {
                            lock.unlock();
                        }
                    }
                    super.onNext(value);
                }
            };
        }

    }

    public static class SleepingStreamObserver<T> extends ForwardingStreamObserver<T> {

        private final CallStreamObserver<T> _callStream;

        public SleepingStreamObserver(CallStreamObserver<T> delegate) {
            super(delegate);
            _callStream = delegate;
        }

        @Override
        public void onNext(T value) {
            try {
                while (!_callStream.isReady()) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            super.onNext(value);
        }
    }

    public static class YieldingStreamObserver<T> extends ForwardingStreamObserver<T> {

        private final CallStreamObserver<T> _callStream;

        public YieldingStreamObserver(CallStreamObserver<T> delegate) {
            super(delegate);
            _callStream = delegate;
        }

        @Override
        public void onNext(T value) {
            while (!_callStream.isReady()) {
                Thread.yield();
            }
            super.onNext(value);
        }
    }
}
