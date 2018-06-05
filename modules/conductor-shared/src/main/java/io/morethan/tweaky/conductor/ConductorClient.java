package io.morethan.tweaky.conductor;

import java.util.List;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorBlockingStub;
import io.morethan.tweaky.conductor.proto.ConductorProto.ServerServicesRequest;
import io.morethan.tweaky.grpc.Errors;

/**
 * A client for {@link ConductorGrpc}.
 */
public class ConductorClient {

    private final ConductorBlockingStub _blockingStub;

    private ConductorClient(ConductorBlockingStub blockingStub) {
        _blockingStub = blockingStub;
    }

    public List<String> serverServices() {
        try {
            return _blockingStub.serverServices(ServerServicesRequest.getDefaultInstance()).getNameList();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static ConductorClient on(Channel channel) {
        return new ConductorClient(ConductorGrpc.newBlockingStub(channel));
    }

}
