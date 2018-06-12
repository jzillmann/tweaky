package io.morethan.tweaky.test;

import java.util.ArrayList;
import java.util.List;

import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;

/**
 * Bundles conductor and n nodes.
 */
public abstract class TestCluster implements AutoCloseable {

    private final int _numberOfNodes;
    private GrpcServer _conductor;
    private final List<GrpcServer> _nodes = new ArrayList<>();
    private final ChannelProvider _channelProvider;

    public TestCluster(int numberOfNodes, ChannelProvider channelProvider) {
        _numberOfNodes = numberOfNodes;
        _channelProvider = channelProvider;
    }

    public GrpcServer conductor() {
        return _conductor;
    }

    public GrpcServer node(int i) {
        return nodes().get(i);
    }

    public List<GrpcServer> nodes() {
        return _nodes;
    }

    protected abstract GrpcServer createConductor();

    protected abstract GrpcServer createNode(int number, int conductorPort, ChannelProvider channelProvider);

    public TestCluster boot() {
        _conductor = createConductor();
        _conductor.startAsync().awaitRunning();

        for (int i = 0; i < _numberOfNodes; i++) {
            GrpcServer node = createNode(i, _conductor.getPort(), _channelProvider);
            node.startAsync();
            _nodes.add(node);
        }
        return this;
    }

    public TestCluster awaitNodes() {
        return awaitNodes(_numberOfNodes);
    }

    public TestCluster awaitNodes(int nodeCount) {
        try (ClosableChannel channel = channelToConductor()) {
            NodeRegistryClient.on(channel).awaitNodes(nodeCount);
        }
        return this;
    }

    public ClosableChannel channelToConductor() {
        return ClosableChannel.of(_channelProvider.get("localhost", _conductor.getPort()));
    }

    @Override
    public void close() {
        for (GrpcServer grpcServer : _nodes) {
            grpcServer.stopAsync().awaitTerminated();
        }
        _conductor.stopAsync().awaitTerminated();
    }
}
