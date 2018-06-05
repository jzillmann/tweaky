package io.morethan.examples.dm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc.GatewayImplBase;
import io.morethan.tweaky.examples.dm.shared.proto.GetReply;
import io.morethan.tweaky.examples.dm.shared.proto.GetRequest;
import io.morethan.tweaky.examples.dm.shared.proto.PutReply;
import io.morethan.tweaky.examples.dm.shared.proto.PutRequest;

/**
 * GRPC service of the Gateway server.
 */
public class GatewayGrpcService extends GatewayImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayGrpcService.class);

    @Override
    public StreamObserver<PutRequest> put(StreamObserver<PutReply> responseObserver) {
        return new StreamObserver<PutRequest>() {

            @Override
            public void onNext(PutRequest value) {
                System.out.println(value);
                // TODO forward to nodes
            }

            @Override
            public void onError(Throwable t) {
                LOG.warn("Client signaled error", t);
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
        System.out.println("GatewayGrpcService.get()");
        super.get(request, responseObserver);
    }
}
