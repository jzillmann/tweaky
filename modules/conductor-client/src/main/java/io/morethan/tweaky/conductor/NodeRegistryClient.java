package io.morethan.tweaky.conductor;

import io.morethan.tweaky.conductor.registration.proto.NodeRegistryGrpc;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryGrpc.NodeRegistryBlockingStub;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryProto.NodeRegistrationRequest;
import io.morethan.tweaky.shared.GrpcClient;

/**
 * A client for {@link ConductorGrpcService}.
 */
public class NodeRegistryClient implements AutoCloseable {

    private final GrpcClient _grpcClient;
    private final NodeRegistryBlockingStub _blockingStub;

    public NodeRegistryClient(GrpcClient grpcClient) {
        _grpcClient = grpcClient;
        _blockingStub = grpcClient.createStub(NodeRegistryGrpc::newBlockingStub);
    }

    public void registerNode(String host, int port, String token) {
        _blockingStub.registerNode(NodeRegistrationRequest.newBuilder().setHost(host).setPort(port).setToken(token).build());
    }

    @Override
    public void close() {
        _grpcClient.close();
    }
}
