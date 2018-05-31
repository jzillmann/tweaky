package io.morethan.tweaky.node;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.node.proto.NodeGrpc;
import io.morethan.tweaky.node.proto.NodeGrpc.NodeImplBase;
import io.morethan.tweaky.node.proto.NodeProto.NodeTokenReply;
import io.morethan.tweaky.node.proto.NodeProto.NodeTokenRequest;
import io.morethan.tweaky.shared.Errors;

/**
 * GRPC service implementation for {@link NodeGrpc}.
 */
public class NodeGrpcService extends NodeImplBase {

    private final String _token;

    public NodeGrpcService(String token) {
        _token = token;
    }

    @Override
    public void token(NodeTokenRequest request, StreamObserver<NodeTokenReply> responseObserver) {
        try {
            responseObserver.onNext(NodeTokenReply.newBuilder().setToken(_token).build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }
}
