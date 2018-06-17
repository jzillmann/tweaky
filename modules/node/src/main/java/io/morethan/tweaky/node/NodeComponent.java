package io.morethan.tweaky.node;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeModule.AutoRegister;
import io.morethan.tweaky.node.NodeModule.NodeRegistryHost;
import io.morethan.tweaky.node.NodeModule.NodeRegistryPort;
import io.morethan.tweaky.node.NodeModule.NodeToken;

/**
 * Main component of the node server.
 */
@Component(modules = { NodeModule.class, GrpcServerModule.class, GrpcServicesModule.class })
@Singleton
public interface NodeComponent {

    GrpcServer server();

    /**
     * Can be used to register the node on the node registry, in case auto-register is disabled.
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
         * The token the node registers itself at the node registry with. Required.
         * 
         * @param nodeToken
         * @return
         */
        @BindsInstance
        Builder token(@NodeToken String nodeToken);

        /**
         * The host of the node registry. Required.
         * 
         * @param nodeRegistryHost
         * @return
         */
        @BindsInstance
        Builder nodeRegistryHost(@NodeRegistryHost String nodeRegistryHost);

        /**
         * The port of the node registry. Required.
         * 
         * @param nodeRegistryPort
         * @return
         */
        @BindsInstance
        Builder nodeRegistryPort(@NodeRegistryPort int nodeRegistryPort);

        @BindsInstance
        Builder autoRegister(@AutoRegister boolean autoRegister);

        NodeComponent build();

    }
}
