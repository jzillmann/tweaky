package io.morethan.examples.dm.gateway;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.examples.dm.gateway.GatewayModule.NodeCount;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.noderegistry.NodeRegistryComponent;
import io.morethan.tweaky.noderegistry.NodeRegistryModule;

@Component(modules = { NodeRegistryModule.class, GrpcServerModule.class, GrpcServicesModule.class, GatewayModule.class })
@Singleton
public interface GatewayComponent extends NodeRegistryComponent {

    public static Builder builder() {
        return DaggerGatewayComponent.builder();
    }

    @Component.Builder
    interface Builder extends NodeRegistryComponent.Builder {

        @BindsInstance
        Builder nodeCount(@NodeCount int nodeCount);

        @Override
        GatewayComponent build();
    }

}
