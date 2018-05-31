package io.morethan.tweaky.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.util.concurrent.AbstractIdleService;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * A GRPC server which can be used to host {@link BindableService} implementations.
 */
public class GrpcServer extends AbstractIdleService {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);
    private final Server _rpcServer;

    public GrpcServer(int port, BindableService... grpcServices) {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        for (BindableService bindableService : grpcServices) {
            serverBuilder.addService(bindableService);
        }
        _rpcServer = serverBuilder.build();
    }

    public int getPort() {
        return _rpcServer.getPort();
    }

    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting GRPC server with following services: {}",
                FluentIterable.from(_rpcServer.getServices()).transform(serviceDefinition -> serviceDefinition.getServiceDescriptor().getName()).toList());
        _rpcServer.start();
        LOG.info("Started GRPC server on port " + _rpcServer.getPort());
    }

    @Override
    protected void shutDown() throws Exception {
        _rpcServer.shutdown();
    }

}
