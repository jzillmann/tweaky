package io.morethan.tweaky.conductor;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationModule;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

/**
 * Main component for conductor server component. Use the
 */
@Component(modules = { ConductorModule.class, NodeRegistrationModule.class, GrpcServicesModule.class, ConductorAppModule.class })
@Singleton
public interface ConductorComponent {

    NodeRegistry nodeRegistry();

    GrpcServer conductorServer();

    public static Builder builder() {
        return DaggerConductorComponent.builder();
    }

    @Component.Builder
    interface Builder {

        /**
         * Specify the {@link GrpcServerModule}. Required.
         * 
         * @param grpcServerModule
         * @return
         */
        Builder grpcServerModule(GrpcServerModule grpcServerModule);

        Builder grpcServiceModule(GrpcServicesModule grpcServiceModule);

        Builder appModule(ConductorAppModule appModule);

        @BindsInstance
        Builder nodeRegistrationValidator(NodeRegistrationValidator nodeRegistrationValidator);

        @BindsInstance
        Builder nodeNameProvider(NodeNameProvider nodeNameProvider);

        ConductorComponent build();
    }

}
