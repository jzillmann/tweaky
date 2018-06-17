package io.morethan.tweaky.grpc.client;

import java.util.List;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.grpc.server.ServiceRegistryGrpcService;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryGrpc;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryGrpc.ServiceRegistryBlockingStub;
import io.morethan.tweaky.grpc.server.proto.ServiceRegistryProto.ServicesRequest;

/**
 * A client for {@link ServiceRegistryGrpcService}.
 */
public class ServiceRegistryClient {

    private final ServiceRegistryBlockingStub _blockingStub;

    private ServiceRegistryClient(ServiceRegistryBlockingStub blockingStub) {
        _blockingStub = blockingStub;
    }

    public List<String> services() {
        try {
            return _blockingStub.serverServices(ServicesRequest.getDefaultInstance()).getNameList();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static ServiceRegistryClient on(Channel channel) {
        return new ServiceRegistryClient(ServiceRegistryGrpc.newBlockingStub(channel));
    }

}
