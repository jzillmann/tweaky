package io.morethan.examples.dm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc.GatewayBlockingStub;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc.GatewayStub;
import io.morethan.tweaky.examples.dm.shared.proto.GetRequest;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;
import io.morethan.tweaky.grpc.Errors;

/**
 * Client for the {@link GatewayGrpc}.
 */
public class GatewayClient {

    static final Logger LOG = LoggerFactory.getLogger(GatewayClient.class);
    private final GatewayStub _stub;
    private final GatewayBlockingStub _blockingStub;

    private GatewayClient(GatewayStub stub, GatewayBlockingStub blockingStub) {
        _stub = stub;
        _blockingStub = blockingStub;
    }

    public Inserter createInserter() {
        CompletableObserver<PutReply> replyStream = new CompletableObserver<>();
        StreamObserver<PutRequest> requestStream = _stub.put(replyStream);
        return new Inserter(requestStream, replyStream);
    }

    public void put(String key, String value) {
        _blockingStub.putSync(PutRequest.newBuilder().setKey(key).setValue(value).build());
    }

    public String get(String key) {
        try {
            return _blockingStub.get(GetRequest.newBuilder().setKey(key).build()).getValue();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static GatewayClient on(Channel channel) {
        return new GatewayClient(GatewayGrpc.newStub(channel), GatewayGrpc.newBlockingStub(channel));
    }
}
