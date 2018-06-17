package io.morethan.tweaky.grpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;

import io.grpc.Server;

/**
 * An {@link Service} wrapping a {@link Server}.
 */
public class GrpcServer extends AbstractIdleService {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);
    private final Server _server;

    public GrpcServer(Server server) {
        _server = server;
    }

    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting GRPC server with following services: {}",
                FluentIterable.from(_server.getServices()).transform(serviceDefinition -> serviceDefinition.getServiceDescriptor().getName()).toList());
        _server.start();
        LOG.info("Started GRPC server on port " + _server.getPort());
    }

    @Override
    protected void shutDown() throws Exception {
        LOG.info("Stopping GRPC server...");
        _server.shutdown();
        LOG.info("Stopped GRPC server");
    }

    public int getPort() {
        return _server.getPort();
    }

    // TODO log server instance with bind host, etc... ?

}
