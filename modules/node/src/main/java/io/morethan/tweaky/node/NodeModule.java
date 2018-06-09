package io.morethan.tweaky.node;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Service.Listener;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule.GrpcServerListener;

@Module
public class NodeModule {

    @Provides
    @Singleton
    @IntoSet
    BindableService nodeService(@NodeToken String token, Lazy<Set<BindableService>> bindableServices) {
        return new NodeGrpcService(token, bindableServices);
    }

    @Provides
    @Singleton
    ManagedChannel conductorChannel(ChannelProvider channelProvider, @ConductorHost String conductorHost, @ConductorPort int conductorPort) {
        return channelProvider.get(conductorHost, conductorPort);
    }

    @Provides
    @Singleton
    NodeRegisterer nodeRegisterer(@NodeToken String token, ManagedChannel conductorChannel, Lazy<GrpcServer> grpcServer) {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return new NodeRegisterer(token, hostName, conductorChannel, grpcServer);
    }

    @Provides
    @Singleton
    @ElementsIntoSet
    @GrpcServerListener
    Set<Listener> grpcServerListener(Lazy<GrpcServer> grpcServer, NodeRegisterer nodeRegisterer, @AutoRegister boolean autoRegister) {
        if (!autoRegister) {
            return Collections.emptySet();
        }
        return ImmutableSet.of(new NodeAutoRegisterer(grpcServer, nodeRegisterer));
    }

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface NodeToken {
    }

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ConductorHost {
    }

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ConductorPort {
    }

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AutoRegister {
    }
}
