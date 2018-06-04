package io.morethan.tweaky.conductor;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

@Module(includes = { GrpcServerModule.class })
public class ConductorModule {

    @Provides
    @Singleton
    @IntoSet
    BindableService conductorService(Lazy<Set<BindableService>> bindableServices) {
        return new ConductorGrpcService(bindableServices);
    }

    @Provides
    @Singleton
    @IntoSet
    BindableService nodeRegistryService(NodeRegistry nodeRegistry) {
        return new NodeRegistryGrpcService(nodeRegistry);
    }

}
