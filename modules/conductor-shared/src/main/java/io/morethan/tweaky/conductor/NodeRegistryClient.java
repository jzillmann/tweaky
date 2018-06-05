package io.morethan.tweaky.conductor;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryGrpc;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryGrpc.NodeRegistryBlockingStub;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryProto.NodeCountRequest;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryProto.NodeRegistrationRequest;
import io.morethan.tweaky.grpc.Errors;

/**
 * A client for {@link ConductorGrpcService}.
 */
public class NodeRegistryClient {

    private final NodeRegistryBlockingStub _blockingStub;

    private NodeRegistryClient(NodeRegistryBlockingStub blockingStub) {
        _blockingStub = blockingStub;
    }

    public void registerNode(String host, int port, String token) {
        try {
            _blockingStub.registerNode(NodeRegistrationRequest.newBuilder().setHost(host).setPort(port).setToken(token).build());
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public int nodeCount() {
        try {
            return _blockingStub.nodeCount(NodeCountRequest.getDefaultInstance()).getCount();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static NodeRegistryClient on(Channel channel) {
        return new NodeRegistryClient(NodeRegistryGrpc.newBlockingStub(channel));
    }
}
