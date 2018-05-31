package io.morethan.tweaky.conductor.registration;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryGrpc.NodeRegistryImplBase;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryProto.NodeRegistrationReply;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryProto.NodeRegistrationRequest;
import io.morethan.tweaky.shared.Errors;

/**
 * A GRPC service for {@link NodeRegistry}.
 */
public class NodeRegistryGrpcService extends NodeRegistryImplBase {

    private final NodeRegistry _nodeRegistry;

    public NodeRegistryGrpcService(NodeRegistry nodeRegistry) {
        _nodeRegistry = nodeRegistry;
    }

    @Override
    public void registerNode(NodeRegistrationRequest request, StreamObserver<NodeRegistrationReply> responseObserver) {
        try {
            _nodeRegistry.registerNode(request.getHost(), request.getPort(), request.getToken());
            responseObserver.onNext(NodeRegistrationReply.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }
}
