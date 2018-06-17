package io.morethan.tweaky.grpc;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import io.grpc.BindableService;

/**
 * A dagger module allowing to register additional GRPC {@link BindableService}s to an existing dagger module.
 */
@Module
public class GrpcServicesModule {

    private final Set<BindableService> _services = new HashSet<>();

    public GrpcServicesModule with(BindableService service) {
        _services.add(service);
        return this;
    }

    @Provides
    @Singleton
    @ElementsIntoSet
    Set<BindableService> services() {
        return _services;
    }

}
