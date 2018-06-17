package io.morethan.tweaky.grpc.server;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dagger.Lazy;
import io.grpc.BindableService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryGrpc;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryGrpc.ServiceRegistryImplBase;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryProto.ServicesReply;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryProto.ServicesRequest;

/**
 * GRPC service implementation for {@link ServiceRegistryGrpc}.
 */
public final class ServiceRegistryGrpcService extends ServiceRegistryImplBase {

    private final Lazy<Set<BindableService>> _bindableServices;

    public ServiceRegistryGrpcService(Lazy<Set<BindableService>> bindableServices) {
        _bindableServices = bindableServices;
    }

    @Override
    public void serverServices(ServicesRequest request, StreamObserver<ServicesReply> responseObserver) {
        try {
            List<String> serviceNames = _bindableServices.get().stream().map((service) -> service.bindService().getServiceDescriptor().getName()).collect(Collectors.toList());
            responseObserver.onNext(ServicesReply.newBuilder().addAllName(serviceNames).build());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Errors.newRpcError(Status.INTERNAL, e));
        }
    }

}
