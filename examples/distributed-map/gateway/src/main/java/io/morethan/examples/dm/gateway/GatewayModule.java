package io.morethan.examples.dm.gateway;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.morethan.tweaky.noderegistry.NodeListener;

@Module
public class GatewayModule {

    @Provides
    @IntoSet
    @Singleton
    BindableService gatewayService(MapNodeClientCache mapNodeClients) {
        return new GatewayGrpcService(mapNodeClients);
    }

    @Provides
    @Singleton
    @IntoSet
    NodeListener mapNodeClientsListener(MapNodeClientCache mapNodeClients) {
        return mapNodeClients;
    }

    @Provides
    @Singleton
    MapNodeClientCache mapNodeClients(@NodeCount int numberOfNodes) {
        return new MapNodeClientCache(numberOfNodes);
    }

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface NodeCount {
    }

}
