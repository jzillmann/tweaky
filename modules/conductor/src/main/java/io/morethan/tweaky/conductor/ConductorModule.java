package io.morethan.tweaky.conductor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.grpc.GrpcServerModule;

@Module(includes = { GrpcServerModule.class })
public class ConductorModule {

    @Provides
    @Singleton
    @IntoSet
    BindableService conductorService(Conductor conductor) {
        return new ConductorGrpcService(conductor);
    }

    @Provides
    @Singleton
    @IntoSet
    BindableService nodeRegistryService(NodeRegistry nodeRegistry) {
        return new NodeRegistryGrpcService(nodeRegistry);
    }

    @Provides
    @Singleton
    Conductor conductor(NodeRegistry nodeRegistry) {
        return new Conductor(nodeRegistry);
    }
}
