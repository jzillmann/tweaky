package io.morethan.tweaky.conductor.channel;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * A {@link NodeChannelProvider} which provides a simple plaintext (un-secure) channel.
 */
public class PlaintextChannelProvider implements ChannelProvider {

    @Override
    public ManagedChannel get(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

}
