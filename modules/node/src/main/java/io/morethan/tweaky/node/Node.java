package io.morethan.tweaky.node;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.shared.GrpcClient;

public class Node extends AbstractIdleService {

    private static final Logger LOG = LoggerFactory.getLogger(Node.class);
    private final int _port;
    private final String _token;
    private final String _conductorHost;
    private final int _conductorPort;

    private Server _nodeServer;
    private ManagedChannel _conductorChannel;

    // TODO rework that...
    public Node(int port, String token, String conductorHost, int conductorPort) {
        _port = port;
        _token = token;
        _conductorHost = conductorHost;
        _conductorPort = conductorPort;
    }

    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting Node on port {} ...", _port);
        // First startup own server
        _nodeServer = ServerBuilder.forPort(_port).addService(new NodeGrpcService(_token)).build();
        _nodeServer.start();

        // Register on conductor
        _conductorChannel = ManagedChannelBuilder.forAddress(_conductorHost, _conductorPort).usePlaintext().build();
        try (NodeRegistryClient nodeRegistryClient = new NodeRegistryClient(GrpcClient.shared(_conductorChannel));) {
            nodeRegistryClient.registerNode(InetAddress.getLocalHost().getHostName(), _nodeServer.getPort(), _token);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        if (_nodeServer != null) {
            _nodeServer.shutdown();
        }
        if (_conductorChannel != null) {
            _conductorChannel.shutdown();
        }
    }

}
