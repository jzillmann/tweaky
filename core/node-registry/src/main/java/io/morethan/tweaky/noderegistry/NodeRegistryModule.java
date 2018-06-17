package io.morethan.tweaky.noderegistry;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.morethan.tweaky.grpc.client.ChannelProvider;

@Module
public class NodeRegistryModule {

    @Provides
    @Singleton
    @IntoSet
    BindableService nodeRegistryService(NodeRegistry nodeRegistry) {
        return new NodeRegistryGrpcService(nodeRegistry);
    }

    @Provides
    @Singleton
    NodeRegistry nodeRegistry(NodeAcceptor nodeAcceptor, Set<NodeListener> nodeListeners) {
        return new NodeRegistry(nodeAcceptor, nodeListeners);
    }

    @Provides
    @Singleton
    NodeAcceptor nodeAcceptor(NodeRegistrationValidator nodeRegistrationValidator, NodeNameProvider nodeNameProvider, ChannelProvider channelProvider, NodeClientProvider nodeClientProvider) {
        return new NodeAcceptor(nodeRegistrationValidator, nodeNameProvider, channelProvider, nodeClientProvider);
    }

    @Provides
    @Singleton
    NodeClientProvider nodeClientProvider() {
        return NodeClientProvider.create();
    }

    @Provides
    @ElementsIntoSet
    @Singleton
    Set<NodeListener> nodeListener() {
        // This makes declaring of listeners optional
        return Collections.emptySet();
    }

}
