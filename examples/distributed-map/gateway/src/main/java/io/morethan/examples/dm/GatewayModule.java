package io.morethan.examples.dm;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.morethan.tweaky.conductor.registration.NodeRegistry;

@Module
public class GatewayModule {

    @Provides
    @IntoSet
    @Singleton
    BindableService gatewayService(NodeRegistry nodeRegistry) {
        return new GatewayGrpcService();
    }

}
