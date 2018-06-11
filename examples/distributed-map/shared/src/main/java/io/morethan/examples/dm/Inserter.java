package io.morethan.examples.dm;

import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;

/**
 * Streams multiple subsequent put requests to the distributed map cluster.
 */
public class Inserter implements AutoCloseable {

    private final StreamObserver<PutRequest> _requestStream;
    private final CompletableObserver<PutReply> _replyStream;

    public Inserter(StreamObserver<PutRequest> requestStream, CompletableObserver<PutReply> replyStream) {
        _requestStream = requestStream;
        _replyStream = replyStream;
    }

    public void put(String key, String value) {
        _replyStream.throwOnError();
        _requestStream.onNext(PutRequest.newBuilder().setKey(key).setValue(value).build());
    }

    @Override
    public void close() {
        try {
            _requestStream.onCompleted();
            _replyStream.awaitCompletion();
        } catch (InterruptedException e) {
            GatewayClient.LOG.warn("Close interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}