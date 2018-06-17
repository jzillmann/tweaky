package io.morethan.tweaky.node;

import dagger.Lazy;
import io.grpc.ManagedChannel;
import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.grpc.server.GrpcServer;

/**
 * Registers the node on a node registry once the node's {@link GrpcServer} has been started successfully.
 */
public class NodeRegisterer {

    private final String _nodeToken;
    private final String _nodeHost;
    private final ManagedChannel _nodeRegistryChannel;
    private final Lazy<GrpcServer> _grpcServer;

    public NodeRegisterer(String nodeToken, String nodeHost, ManagedChannel nodeRegistryChannel, Lazy<GrpcServer> grpcServer) {
        _nodeToken = nodeToken;
        _nodeHost = nodeHost;
        _nodeRegistryChannel = nodeRegistryChannel;
        _grpcServer = grpcServer;
    }

    public void register() {
        // TODO better way getting host address ?
        // TODO can port be provided by the GrpcServerModule ?
        NodeRegistryClient.on(_nodeRegistryChannel).registerNode(_nodeHost, _grpcServer.get().getPort(), _nodeToken);

    }

}
