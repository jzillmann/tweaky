package io.morethan.tweaky.conductor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dagger.Lazy;
import io.grpc.BindableService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorImplBase;
import io.morethan.tweaky.conductor.proto.ConductorProto.ServerServicesReply;
import io.morethan.tweaky.conductor.proto.ConductorProto.ServerServicesRequest;
import io.morethan.tweaky.grpc.Errors;

/**
 * GRPC service implementation for {@link ConductorGrpc}.
 */
public final class ConductorGrpcService extends ConductorImplBase {

    private final Lazy<Set<BindableService>> _bindableServices;

    public ConductorGrpcService(Lazy<Set<BindableService>> bindableServices) {
        _bindableServices = bindableServices;
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
