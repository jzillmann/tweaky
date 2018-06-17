package io.morethan.tweaky.grpc.server;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Set;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service.Listener;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.morethan.tweaky.grpc.client.ChannelProvider;

/**
 * Abstract module for creating a {@link Server} instance.
 */
@Module
public class GrpcServerModule {

    @Provides
    @Singleton
    static Server server(ServerBuilder<?> serverBuilder, Set<BindableService> bindableServices) {
        for (BindableService bindableService : bindableServices) {
            serverBuilder.addService(bindableService);
        }
        return serverBuilder.build();
    }

    @Provides
    @Singleton
    @IntoSet
    BindableService serviceRegistry(Lazy<Set<BindableService>> bindableServices) {
        return new ServiceRegistryGrpcService(bindableServices);
    }

    @Provides
    @Singleton
    ServerBuilder<?> serverBuilder() {
        throw new UnsupportedOperationException();
    }

    @Provides
    @Singleton
    ChannelProvider channelProvider() {
        throw new UnsupportedOperationException();
    }

    @Provides
    @ElementsIntoSet
    @Singleton
    @GrpcServerListener
    Set<Listener> grpcServerListener() {
        // This makes declaring of listeners optional
        return Collections.emptySet();
    }

    @Provides
    @Singleton
    GrpcServer grpcServer(Server server, @GrpcServerListener Set<Listener> listeners) {
        GrpcServer grpcServer = new GrpcServer(server);
        for (Listener listener : listeners) {
            grpcServer.addListener(listener, MoreExecutors.directExecutor());
        }
        return grpcServer;
    }

    public static GrpcServerModule inProcess(String name) {
        return new GrpcInProcessServerModule(name);
    }

    public static GrpcServerModule plaintext(int port) {
        return new GrpcPlaintextServerModule(port);
    }

    public static GrpcServerModule plaintext(String bindHost, int port) {
        return new GrpcPlaintextServerModule(bindHost, port);
    }

    // TODO ssl ?

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface GrpcServerListener {

    }

}
