package io.morethan.tweaky.node;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dagger.Lazy;
import io.grpc.BindableService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.node.proto.NodeGrpc;
import io.morethan.tweaky.node.proto.NodeGrpc.NodeImplBase;
import io.morethan.tweaky.node.proto.NodeProto.NodeTokenReply;
import io.morethan.tweaky.node.proto.NodeProto.NodeTokenRequest;
import io.morethan.tweaky.node.proto.NodeProto.ServerServicesReply;
import io.morethan.tweaky.node.proto.NodeProto.ServerServicesRequest;

/**
 * GRPC service implementation for {@link NodeGrpc}.
 */
public class NodeGrpcService extends NodeImplBase {

    private final String _token;
    private final Lazy<Set<BindableService>> _bindableServices;

    public NodeGrpcService(String token, Lazy<Set<BindableService>> bindableServices) {
        _token = token;
        _bindableServices = bindableServices;
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

    @Override
    public void serverServices(ServerServicesRequest request, StreamObserver<ServerServicesReply> responseObserver) {
        try {
            List<String> serviceNames = _bindableServices.get().stream().map((service) -> service.bindService().getServiceDescriptor().getName()).collect(Collectors.toList());
            responseObserver.onNext(ServerServicesReply.newBuilder().addAllName(serviceNames).build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }

}
