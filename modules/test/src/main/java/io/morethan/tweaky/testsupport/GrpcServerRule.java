package io.morethan.tweaky.testsupport;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.grpc.BindableService;
import io.morethan.tweaky.grpc.GrpcClient;
import io.morethan.tweaky.test.GrpcServer;

/**
 * A {@link ExternalResource} for {@link GrpcServer}.
 */
public class GrpcServerRule extends ExternalResource {

    private final GrpcServer _grpcServer;

    public GrpcServerRule(BindableService... grpcServices) {
        _grpcServer = new GrpcServer(0, grpcServices);
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

    public GrpcClient newStandaloneClient() {
        return GrpcClient.standalone("localhost", _grpcServer.getPort());
    }

}
