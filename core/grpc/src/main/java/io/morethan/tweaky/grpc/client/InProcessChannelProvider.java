package io.morethan.tweaky.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.morethan.tweaky.grpc.server.GrpcInProcessServerModule;

/**
 * A {@link ChannelProvider} connecting to a in-process server.
 * 
 * @see GrpcInProcessServerModule
 */
public class InProcessChannelProvider implements ChannelProvider {

    private final String _name;

    public InProcessChannelProvider(String name) {
        _name = name;
    }

    @Override
    public ManagedChannel get(String host, int port) {
        return InProcessChannelBuilder.forName(_name).build();
    }

}
