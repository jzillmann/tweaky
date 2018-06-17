package io.morethan.tweaky.noderegistry;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryGrpc.NodeRegistryImplBase;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.AwaitNodesReply;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.AwaitNodesRequest;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.NodeCountReply;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.NodeCountRequest;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.NodeRegistrationReply;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.NodeRegistrationRequest;
import io.morethan.tweaky.noderegistry.util.Counter;

/**
 * A GRPC service for {@link NodeRegistry}.
 */
public class NodeRegistryGrpcService extends NodeRegistryImplBase {

    private final NodeRegistry _nodeRegistry;
    private final Counter _registeredNodes = new Counter();

    public NodeRegistryGrpcService(NodeRegistry nodeRegistry) {
        _nodeRegistry = nodeRegistry;
    }

    @Override
    public void registerNode(NodeRegistrationRequest request, StreamObserver<NodeRegistrationReply> responseObserver) {
        try {
            _nodeRegistry.registerNode(request.getHost(), request.getPort(), request.getToken());
            _registeredNodes.increment();
            responseObserver.onNext(NodeRegistrationReply.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }

    @Override
    public void nodeCount(NodeCountRequest request, StreamObserver<NodeCountReply> responseObserver) {
        try {
            int registeredNodes = _nodeRegistry.registeredNodes();
            responseObserver.onNext(NodeCountReply.newBuilder().setCount(registeredNodes).build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }

    @Override
    public void awaitNodes(AwaitNodesRequest request, StreamObserver<AwaitNodesReply> responseObserver) {
        int count = request.getCount();
        try {
            _registeredNodes.awaitAtLeastCount(count);
            responseObserver.onNext(AwaitNodesReply.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }
    }

}
