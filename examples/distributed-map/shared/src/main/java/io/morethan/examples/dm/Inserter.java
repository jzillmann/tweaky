package io.morethan.examples.dm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;
import io.morethan.tweaky.grpc.observer.CompletableObserver;

/**
 * Streams multiple subsequent put requests to the distributed map cluster.
 */
public class Inserter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Inserter.class);

    private final StreamObserver<PutRequest> _requestStream;
    private final CompletableObserver<PutReply> _replyStream;

    public Inserter(StreamObserver<PutRequest> requestStream, CompletableObserver<PutReply> responseStream) {
        _requestStream = requestStream;
        _replyStream = responseStream;
    }

    public void put(String key, String value) {
        put(PutRequest.newBuilder().setKey(key).setValue(value).build());
    }

    public void put(PutRequest putRequest) {
        _replyStream.throwOnError();
        _requestStream.onNext(putRequest);
    }

    @Override
    public void close() {
        try {
            _requestStream.onCompleted();
            _replyStream.awaitCompletion();
        } catch (InterruptedException e) {
            LOG.warn("Close interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}