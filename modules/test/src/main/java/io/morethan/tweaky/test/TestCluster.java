package io.morethan.tweaky.test;

import java.util.ArrayList;
import java.util.List;

import io.morethan.tweaky.grpc.server.GrpcServer;

/**
 * Bundles conductor and n nodes.
 */
public abstract class TestCluster implements AutoCloseable {

    private final int _numberOfNodes;
    private GrpcServer _conductor;
    private final List<GrpcServer> _nodes = new ArrayList<>();

    public TestCluster(int numberOfNodes) {
        _numberOfNodes = numberOfNodes;
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

    protected abstract GrpcServer createNode(int number, int conductorPort);

    public void boot() {
        _conductor = createConductor();
        _conductor.startAsync().awaitRunning();

        for (int i = 0; i < _numberOfNodes; i++) {
            GrpcServer node = createNode(i, _conductor.getPort());
            node.startAsync();
            _nodes.add(node);
        }
    }

    @Override
    public void close() {
        for (GrpcServer grpcServer : _nodes) {
            grpcServer.stopAsync().awaitTerminated();
        }
        _conductor.stopAsync().awaitTerminated();
    }
}
