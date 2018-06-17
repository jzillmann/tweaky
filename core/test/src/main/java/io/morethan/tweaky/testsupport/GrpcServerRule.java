package io.morethan.tweaky.testsupport;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.server.GrpcServer;

/**
 * A {@link ExternalResource} for {@link GrpcServer}.
 */
public class GrpcServerRule extends ExternalResource {

    private final GrpcServer _grpcServer;

    public GrpcServerRule(BindableService... grpcServices) {
        _grpcServer = new GrpcServer(createServer(grpcServices));
    }

    public int getPort() {
        return _grpcServer.getPort();
    }

    @Override
    protected void before(ExtensionContext context) throws Exception {
        _grpcServer.startAsync().awaitRunning();
    }

    @Override
    protected void after(ExtensionContext context) throws Exception {
        _grpcServer.stopAsync().awaitTerminated();
    }

    public ManagedChannel newChannel() {
        return ChannelProvider.plaintext().get("localhost", _grpcServer.getPort());
    }

    private Server createServer(BindableService[] grpcServices) {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(0);
        for (BindableService bindableService : grpcServices) {
            serverBuilder.addService(bindableService);
        }
        return serverBuilder.build();
    }

}
