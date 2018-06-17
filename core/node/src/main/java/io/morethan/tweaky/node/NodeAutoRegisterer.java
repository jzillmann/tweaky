package io.morethan.tweaky.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service.Listener;

import dagger.Lazy;
import io.morethan.tweaky.grpc.server.GrpcServer;

/**
 * A {@link Listener} that will register the node once the node's {@link GrpcServer} is up and running.
 */
public class NodeAutoRegisterer extends Listener {

    private static final Logger LOG = LoggerFactory.getLogger(NodeAutoRegisterer.class);
    private final Lazy<GrpcServer> _grpcServer;
    private final NodeRegisterer _nodeRegisterer;

    public NodeAutoRegisterer(Lazy<GrpcServer> grpcServer, NodeRegisterer nodeRegisterer) {
        _grpcServer = grpcServer;
        _nodeRegisterer = nodeRegisterer;
    }

    @Override
    public void running() {
        try {
            _nodeRegisterer.register();
        } catch (Exception e) {
            // TODO find a better more visible way in shutting the node down.
            _grpcServer.get().stopAsync();
            LOG.error("Failed to register on node registry. Shutting node down...", e);
        }
    }

}
