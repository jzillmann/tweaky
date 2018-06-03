package io.morethan.tweaky.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

/**
 * A {@link GrpcServerModule} which creates a in-process {@link Server}. Useful for testing.
 */
public class GrpcInProcessServerModule extends GrpcServerModule {

    private final String _name;

    public GrpcInProcessServerModule(String name) {
        _name = name;
    }

    @Override
    ServerBuilder<?> serverBuilder() {
        return InProcessServerBuilder.forName(_name);
    }

}
