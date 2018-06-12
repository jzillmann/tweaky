package io.morethan.tweaky.examples.dm.node;

import javax.inject.Singleton;

import dagger.Component;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeComponent;
import io.morethan.tweaky.node.NodeModule;

/**
 * Main component of the node server.
 */
@Component(modules = { NodeModule.class, GrpcServerModule.class, GrpcServicesModule.class, MapNodeModule.class })
@Singleton
public interface MapNodeComponent extends NodeComponent {

    public static Builder builder() {
        return DaggerMapNodeComponent.builder();
    }

    @Component.Builder
    interface Builder extends NodeComponent.Builder {

        @Override
        MapNodeComponent build();

    }
}
