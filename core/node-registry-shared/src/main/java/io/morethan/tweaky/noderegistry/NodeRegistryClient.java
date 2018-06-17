package io.morethan.tweaky.noderegistry;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryGrpc;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryGrpc.NodeRegistryBlockingStub;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.AwaitNodesRequest;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.NodeCountRequest;
import io.morethan.tweaky.noderegistry.proto.NodeRegistryProto.NodeRegistrationRequest;

/**
 * A client for {@link NodeRegistryGrpc}.
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

    /**
     * Waits until the given number of nodes have joined the cluster.
     * 
     * @param nodeCount
     */
    public void awaitNodes(int nodeCount) {
        try {
            _blockingStub.awaitNodes(AwaitNodesRequest.newBuilder().setCount(nodeCount).build());
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static NodeRegistryClient on(Channel channel) {
        return new NodeRegistryClient(NodeRegistryGrpc.newBlockingStub(channel));
    }
}
