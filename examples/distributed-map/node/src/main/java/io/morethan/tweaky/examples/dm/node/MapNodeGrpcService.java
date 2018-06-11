package io.morethan.tweaky.examples.dm.node;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.node.proto.MapNodeGrpc.MapNodeImplBase;
import io.morethan.tweaky.examples.dm.shared.proto.GetReply;
import io.morethan.tweaky.examples.dm.shared.proto.GetRequest;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;

public class MapNodeGrpcService extends MapNodeImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(MapNodeGrpcService.class);
    private final ConcurrentMap<String, String> _map;

    public MapNodeGrpcService(ConcurrentMap<String, String> map) {
        _map = map;
    }

    @Override
    public StreamObserver<PutRequest> put(StreamObserver<PutReply> responseObserver) {
        return new StreamObserver<PutRequest>() {

            @Override
            public void onNext(PutRequest request) {
                _map.put(request.getKey(), request.getValue());
            }

            @Override
            public void onError(Throwable t) {
                LOG.warn("Gateway signaled error", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(PutReply.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetReply> responseObserver) {
        responseObserver.onNext(GetReply.newBuilder().setValue(_map.get(request.getKey())).build());
        responseObserver.onCompleted();
    }
}
