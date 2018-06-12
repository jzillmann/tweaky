package io.morethan.tweaky.conductor.registration;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import io.morethan.tweaky.conductor.channel.DefaulftNodeChannelProvider;
import io.morethan.tweaky.conductor.channel.NodeChannelProvider;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.PlaintextChannelProvider;

@Module
public class NodeRegistrationModule {

    // TODO add NodeAcceptor

    @Provides
    @Singleton
    NodeRegistry nodeRegistry(NodeRegistrationValidator nodeRegistrationValidator, NodeNameProvider nodeNameProvider, NodeChannelProvider nodeChannelProvider, Set<NodeListener> nodeListeners) {
        return new NodeRegistry(nodeRegistrationValidator, nodeNameProvider, nodeChannelProvider, nodeListeners);
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

    @Provides
    @ElementsIntoSet
    @Singleton
    Set<NodeListener> nodeListener() {
        // This makes declaring of listeners optional
        return Collections.emptySet();
    }

}
