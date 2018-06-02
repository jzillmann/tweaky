package io.morethan.tweaky.conductor.channel;

import io.grpc.ManagedChannel;

/**
 * Creates a {@link ManagedChannel} to a given host:port.
 */
public interface ChannelProvider {

    ManagedChannel get(String host, int port);
}
