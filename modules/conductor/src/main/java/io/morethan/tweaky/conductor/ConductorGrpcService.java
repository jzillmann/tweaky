package io.morethan.tweaky.conductor;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorImplBase;
import io.morethan.tweaky.conductor.proto.ConductorProto.NodeCountReply;
import io.morethan.tweaky.conductor.proto.ConductorProto.NodeCountRequest;
import io.morethan.tweaky.shared.Errors;

/**
 * GRPC service implementation for {@link ConductorGrpc}.
 */
public final class ConductorGrpcService extends ConductorImplBase {

    private final Conductor _conductor;

    public ConductorGrpcService(Conductor conductor) {
        _conductor = conductor;
    }

    @Override
    public void nodeCount(NodeCountRequest request, StreamObserver<NodeCountReply> responseObserver) {
        try {
            responseObserver.onNext(NodeCountReply.newBuilder().setCount(_conductor.nodeCount()).build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }

}
