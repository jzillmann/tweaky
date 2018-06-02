package io.morethan.tweaky.conductor.channel;

import io.grpc.ManagedChannel;

/**
 * Default {@link NodeChannelProvider} constructing {@link ManagedChannel} with help of a given {@link ChannelProvider}.
 */
public class DefaulftNodeChannelProvider implements NodeChannelProvider {

    private final ChannelProvider _channelProvider;

    public DefaulftNodeChannelProvider(ChannelProvider channelProvider) {
        _channelProvider = channelProvider;
    }

    @Override
    public NodeChannel get(String host, int port) {
        return new NodeChannel(_channelProvider.get(host, port));
    }

}
