package io.morethan.tweaky.conductor;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationModule;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

/**
 * Main component of the conductor.
 */
@Component(modules = { ConductorModule.class, NodeRegistrationModule.class, GrpcServerModule.class, GrpcServicesModule.class })
@Singleton
public interface ConductorComponent {

    GrpcServer server();

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

        /**
         * Specify additional GRPC services for registration. Optional.
         * 
         * @param grpcServiceModule
         * @return
         */
        Builder grpcServiceModule(GrpcServicesModule grpcServiceModule);

        /**
         * A custom {@link NodeRegistrationValidator}. Required.
         * 
         * @param nodeRegistrationValidator
         * @return
         */
        @BindsInstance
        Builder nodeRegistrationValidator(NodeRegistrationValidator nodeRegistrationValidator);

        /**
         * A custom {@link NodeNameProvider}. Required.
         * 
         * @param nodeNameProvider
         * @return
         */
        @BindsInstance
        Builder nodeNameProvider(NodeNameProvider nodeNameProvider);

        ConductorComponent build();
    }

}
