package io.morethan.examples.dm.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import io.grpc.stub.StreamObserver;
import io.morethan.examples.dm.Inserter;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc.GatewayImplBase;
import io.morethan.tweaky.examples.dm.shared.proto.GetReply;
import io.morethan.tweaky.examples.dm.shared.proto.GetRequest;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;
import io.morethan.tweaky.grpc.Errors;

/**
 * GRPC service of the Gateway server.
 */
public class GatewayGrpcService extends GatewayImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayGrpcService.class);
    private final MapNodeClientCache _clientCache;

    public GatewayGrpcService(MapNodeClientCache mapNodeClients) {
        _clientCache = mapNodeClients;
    }

    @Override
    public void putSync(PutRequest request, StreamObserver<PutReply> responseObserver) {
        try {
            int hostingNode = hostingNode(request.getKey());
            _clientCache.client(hostingNode).put(request);
            responseObserver.onNext(PutReply.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<PutRequest> put(StreamObserver<PutReply> responseObserver) {
        Inserter[] inserters = new Inserter[_clientCache.clientCount()];
        return new StreamObserver<PutRequest>() {

            @Override
            public void onNext(PutRequest request) {
                int node = hostingNode(request.getKey());
                Inserter inserter = inserters[node];
                if (inserter == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Create inserter for node " + node);
                    }
                    inserter = _clientCache.client(node).createInserter();
                    inserters[node] = inserter;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Putting {}={} into node {}", request.getKey(), request.getValue(), node);
                }
                inserter.put(request);
            }

            @Override
            public void onError(Throwable t) {
                if (Errors.isCancelled(t)) {
                    LOG.warn("Gateway client cancelled the call: " + t.getMessage());
                } else {
                    LOG.error("Gateway client signaled error", t);
                }
            }

            @Override
            public void onCompleted() {
                for (Inserter inserter : inserters) {
                    if (inserter != null) {
                        inserter.close();
                    }
                }
                responseObserver.onNext(PutReply.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetReply> responseObserver) {
        int hostingNode = hostingNode(request.getKey());

        // TODO forward the protocol buffer implementations instead of unwrapping and wrapping
        String value = _clientCache.client(hostingNode).get(request.getKey());
        responseObserver.onNext(GetReply.newBuilder().setValue(value).build());
        responseObserver.onCompleted();
    }

    private int hostingNode(String key) {
        return Hashing.consistentHash(key.hashCode(), _clientCache.clientCount());
    }
}
