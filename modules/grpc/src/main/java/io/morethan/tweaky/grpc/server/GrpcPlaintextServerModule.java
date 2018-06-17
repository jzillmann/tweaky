package io.morethan.tweaky.grpc.server;

import java.net.InetSocketAddress;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.morethan.tweaky.grpc.client.ChannelProvider;

/**
 * A {@link GrpcServerModule} which creates a {@link Server} in simple plaintext mode (no SSL).
 */
public class GrpcPlaintextServerModule extends GrpcServerModule {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcPlaintextServerModule.class);

    private final Optional<String> _bindAddress;
    private final int _port;

    public GrpcPlaintextServerModule(String bindAddress, int port) {
        this(Optional.of(bindAddress), port);
    }

    public GrpcPlaintextServerModule(int port) {
        this(Optional.empty(), port);
    }

    private GrpcPlaintextServerModule(Optional<String> bindAddress, int port) {
        _bindAddress = bindAddress;
        _port = port;
    }

    @Override
    ServerBuilder<?> serverBuilder() {
        if (_bindAddress.isPresent()) {
            LOG.info("Creating server for bind address {} and port {}", _bindAddress.get(), _port);
            return NettyServerBuilder.forAddress(new InetSocketAddress(_bindAddress.get(), _port));
        }
        LOG.info("Creating server for port {}", _port);
        return ServerBuilder.forPort(_port);
    }

    @Override
    ChannelProvider channelProvider() {
        return ChannelProvider.plaintext();
    }

}
