package io.morethan.examples.dm;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.node.proto.MapNodeGrpc;
import io.morethan.tweaky.examples.dm.node.proto.MapNodeGrpc.MapNodeBlockingStub;
import io.morethan.tweaky.examples.dm.node.proto.MapNodeGrpc.MapNodeStub;
import io.morethan.tweaky.examples.dm.shared.proto.GetRequest;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;
import io.morethan.tweaky.grpc.Errors;

/**
 * Client for the {@link MapNodeGrpc}.
 */
public class MapNodeClient {

    private final MapNodeStub _stub;
    private final MapNodeBlockingStub _blockingStub;

    public MapNodeClient(MapNodeStub stub, MapNodeBlockingStub blockingStub) {
        _stub = stub;
        _blockingStub = blockingStub;
    }

    public Inserter createInserter() {
        CompletableObserver<PutReply> replyStream = new CompletableObserver<>();
        StreamObserver<PutRequest> requestStream = _stub.put(replyStream);
        return new Inserter(requestStream, replyStream);
    }

    public void put(String key, String value) {
        put(PutRequest.newBuilder().setKey(key).setValue(value).build());
    }

    public void put(PutRequest putRequest) {
        _blockingStub.putSync(putRequest);
    }

    public String get(String key) {
        try {
            return _blockingStub.get(GetRequest.newBuilder().setKey(key).build()).getValue();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static MapNodeClient on(Channel channel) {
        return new MapNodeClient(MapNodeGrpc.newStub(channel), MapNodeGrpc.newBlockingStub(channel));
    }
}
