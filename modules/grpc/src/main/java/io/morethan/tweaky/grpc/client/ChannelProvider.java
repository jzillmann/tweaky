package io.morethan.tweaky.grpc.client;

import io.grpc.ManagedChannel;

/**
 * Creates a {@link ManagedChannel} to a given host:port.
 */
public interface ChannelProvider {

    ManagedChannel get(String host, int port);

    public static ChannelProvider plaintext() {
        return new PlaintextChannelProvider();
    }

    public static ChannelProvider inProcess(String name) {
        return new InProcessChannelProvider(name);
    }

}
