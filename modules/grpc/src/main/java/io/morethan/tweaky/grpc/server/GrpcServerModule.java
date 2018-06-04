package io.morethan.tweaky.grpc.server;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

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
    ServerBuilder<?> serverBuilder() {
        throw new UnsupportedOperationException();
    }

    @Provides
    @Singleton
    GrpcServer grpcServer(Server server) {
        return new GrpcServer(server);
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

}
