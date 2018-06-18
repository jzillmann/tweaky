package io.morethan.tweaky.test.cluster;

import java.util.ArrayList;
import java.util.List;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.noderegistry.NodeRegistryClient;

/**
 * Bundles node registry and n nodes.
 */
public abstract class TestCluster implements AutoCloseable {

    private final int _numberOfNodes;
    private GrpcServer _nodeRegistryServer;
    private final List<GrpcServer> _nodes = new ArrayList<>();
    private final ChannelProvider _channelProvider;

    public TestCluster(int numberOfNodes, ChannelProvider channelProvider) {
        _numberOfNodes = numberOfNodes;
        _channelProvider = channelProvider;
    }

    public int numberOfNodes() {
        return _numberOfNodes;
    }

    public GrpcServer nodeRegistryServer() {
        return _nodeRegistryServer;
    }

    public GrpcServer node(int i) {
        return nodes().get(i);
    }

    public List<GrpcServer> nodes() {
        return _nodes;
    }

    protected abstract GrpcServer createNodeRegistry();

    protected abstract GrpcServer createNode(int number, int nodeRegistryPort);

    public TestCluster boot() {
        _nodeRegistryServer = createNodeRegistry();
        _nodeRegistryServer.startAsync().awaitRunning();

        for (int i = 0; i < _numberOfNodes; i++) {
            GrpcServer node = createNode(i, _nodeRegistryServer.getPort());
            node.startAsync();
            _nodes.add(node);
        }
        return this;
    }

    public TestCluster awaitNodes() {
        return awaitNodes(_numberOfNodes);
    }

    public TestCluster awaitNodes(int nodeCount) {
        try (ClosableChannel channel = channelToNodeRegistry()) {
            NodeRegistryClient.on(channel).awaitNodes(nodeCount);
        }
        return this;
    }

    public ClosableChannel channelToNodeRegistry() {
        return ClosableChannel.of(_channelProvider.get("localhost", _nodeRegistryServer.getPort()));
    }

    @Override
    public void close() {
        for (GrpcServer grpcServer : _nodes) {
            grpcServer.stopAsync().awaitTerminated();
        }
        _nodeRegistryServer.stopAsync().awaitTerminated();
    }

}
