package io.morethan.tweaky.conductor;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import io.grpc.BindableService;
import io.morethan.tweaky.conductor.registration.NodeRegistry;

@Module
public class ConductorAppModule {

    @Provides
    @Singleton
    @ElementsIntoSet
    public Set<BindableService> services(NodeRegistry nodeRegistry) {
        // subclasses may override
        return Collections.emptySet();
    }

}
