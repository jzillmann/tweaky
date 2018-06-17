package io.morethan.tweaky.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.InProcessChannelProvider;

/**
 * A {@link GrpcServerModule} which creates a in-process {@link Server}. Useful for testing.
 *
 * @see InProcessChannelProvider for client creation
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

    @Override
    ChannelProvider channelProvider() {
        return ChannelProvider.inProcess(_name);
    }

}
