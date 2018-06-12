package io.morethan.examples.dm.gateway;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.examples.dm.gateway.GatewayModule.NodeCount;
import io.morethan.tweaky.conductor.ConductorComponent;
import io.morethan.tweaky.conductor.ConductorModule;
import io.morethan.tweaky.conductor.registration.NodeRegistrationModule;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

@Component(modules = { ConductorModule.class, NodeRegistrationModule.class, GrpcServerModule.class, GrpcServicesModule.class, GatewayModule.class })
@Singleton
public interface GatewayComponent extends ConductorComponent {

    public static Builder builder() {
        return DaggerGatewayComponent.builder();
    }

    @Component.Builder
    interface Builder extends ConductorComponent.Builder {

        @BindsInstance
        Builder nodeCount(@NodeCount int nodeCount);

        @Override
        GatewayComponent build();
    }

}
