package io.morethan.tweaky.node;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeModule.AutoRegister;
import io.morethan.tweaky.node.NodeModule.ConductorHost;
import io.morethan.tweaky.node.NodeModule.ConductorPort;
import io.morethan.tweaky.node.NodeModule.NodeToken;

/**
 * Main component of the node server.
 */
@Component(modules = { NodeModule.class, GrpcServerModule.class, GrpcServicesModule.class })
@Singleton
public interface NodeComponent {

    GrpcServer server();

    /**
     * Can be used to register the node on the conductor, in case auto-register is disabled.
     * 
     * @return
     */
    NodeRegisterer nodeRegisterer();

    public static Builder builder() {
        return DaggerNodeComponent.builder();
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
         * The token the node registers itself at the conductor with. Required.
         * 
         * @param nodeToken
         * @return
         */
        @BindsInstance
        Builder token(@NodeToken String nodeToken);

        /**
         * The host of the conductor. Required.
         * 
         * @param conductorHost
         * @return
         */
        @BindsInstance
        Builder conductorHost(@ConductorHost String conductorHost);

        /**
         * The port of the conductor. Required.
         * 
         * @param conductorPort
         * @return
         */
        @BindsInstance
        Builder conductorPort(@ConductorPort int conductorPort);

        @BindsInstance
        Builder autoRegister(@AutoRegister boolean autoRegister);

        NodeComponent build();

    }
}
