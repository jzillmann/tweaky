package io.morethan.examples.dm;

import java.util.HashSet;
import java.util.Set;

import dagger.Provides;
import io.grpc.BindableService;
import io.morethan.tweaky.conductor.ConductorAppModule;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistry;

public class GatewayModule extends ConductorAppModule {

    @Override
    public Set<BindableService> services(NodeRegistry nodeRegistry) {
        Set<BindableService> services = new HashSet<>();
        services.add(gatewayService());
        System.out.println("GatewayModule.services()");
        return services;
    }

    BindableService gatewayService() {
        System.out.println("GatewayModule.gatewayService()");
        return new GatewayGrpcService();
    }

    @Provides
    BindableService doesNotWork(NodeNameProvider nodeNameProvider) {
        System.out.println("GatewayModule.doesNotWork():" + nodeNameProvider);
        return new GatewayGrpcService();
    }

}
