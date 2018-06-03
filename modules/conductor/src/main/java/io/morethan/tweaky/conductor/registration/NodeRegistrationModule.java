package io.morethan.tweaky.conductor.registration;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.morethan.tweaky.conductor.channel.ChannelProvider;
import io.morethan.tweaky.conductor.channel.DefaulftNodeChannelProvider;
import io.morethan.tweaky.conductor.channel.NodeChannelProvider;
import io.morethan.tweaky.conductor.channel.PlaintextChannelProvider;

@Module
public class NodeRegistrationModule {

    // TODO add NodeAcceptor

    @Provides
    @Singleton
    NodeRegistry nodeRegistry(NodeRegistrationValidator nodeRegistrationValidator, NodeNameProvider nodeNameProvider, NodeChannelProvider nodeChannelProvider) {
        return new NodeRegistry(nodeRegistrationValidator, nodeNameProvider, nodeChannelProvider);
    }

    @Provides
    @Singleton
    ChannelProvider channelProvider() {
        // TODO lookup based on configuration
        return new PlaintextChannelProvider();
    }

    @Provides
    @Singleton
    NodeChannelProvider nodeChannelProvider(ChannelProvider channelProvider) {
        return new DefaulftNodeChannelProvider(channelProvider);
    }

}
