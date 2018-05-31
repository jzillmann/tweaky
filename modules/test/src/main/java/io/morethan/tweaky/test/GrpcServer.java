package io.morethan.tweaky.test;

import com.google.common.util.concurrent.AbstractIdleService;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * A GRPC server which can be used to host {@link BindableService} implementations.
 */
public class GrpcServer extends AbstractIdleService {

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
        _rpcServer.start();
    }

    @Override
    protected void shutDown() throws Exception {
        _rpcServer.shutdown();
    }

}
