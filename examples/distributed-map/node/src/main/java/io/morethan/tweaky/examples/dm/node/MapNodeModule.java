package io.morethan.tweaky.examples.dm.node;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;

@Module
public class MapNodeModule {

    @Provides
    @Singleton
    ConcurrentMap<String, String> mapImplementation() {
        return new ConcurrentHashMap<>();
    }

    @Provides
    @IntoSet
    @Singleton
    BindableService mapNodeService(ConcurrentMap<String, String> map) {
        return new MapNodeGrpcService(map);
    }

}
