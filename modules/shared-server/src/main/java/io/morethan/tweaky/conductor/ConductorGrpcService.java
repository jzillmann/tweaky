package io.morethan.tweaky.conductor;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.proto.ConductorProto.NodeCountReply;
import io.morethan.tweaky.conductor.proto.ConductorProto.NodeCountRequest;

/**
 * GRPC service implementation for {@link ConductorGrpc}.
 */
public final class ConductorGrpcService extends ConductorGrpc.ConductorImplBase {
    // TODO package private ?

    @Override
    public void nodeCount(NodeCountRequest request, StreamObserver<NodeCountReply> responseObserver) {
        try {
            int nodeCount = 23;
            responseObserver.onNext(NodeCountReply.newBuilder().setCount(nodeCount).build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }
}
