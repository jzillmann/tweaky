package io.morethan.tweaky.conductor;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationModule;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.grpc.GrpcServer;
import io.morethan.tweaky.grpc.GrpcServerModule;

/**
 * Main component for conductor server component. Use the
 */
@Component(modules = { ConductorModule.class, NodeRegistrationModule.class })
@Singleton
public interface ConductorComponent {

    NodeRegistry nodeRegistry();

    Conductor conductor();

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

        @BindsInstance
        Builder nodeRegistrationValidator(NodeRegistrationValidator nodeRegistrationValidator);

        @BindsInstance
        Builder nodeNameProvider(NodeNameProvider nodeNameProvider);

        ConductorComponent build();
    }

}
