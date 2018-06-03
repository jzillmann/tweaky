package io.morethan.tweaky.grpc;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;

/**
 * An wrapper around {@link ManagedChannel} which abstracts the closing of the channel and thus allows handling of shared or standalone clients in the same way.
 */
public class GrpcClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcClient.class);

    private final ManagedChannel _channel;
    private long _shutdownTimeout = 5;
    private TimeUnit _shutdownTimeoutUnit = TimeUnit.SECONDS;

    public GrpcClient(ManagedChannel channel) {
        _channel = channel;
    }

    public void setShutdownTimeout(long shutdownTimeout) {
        _shutdownTimeout = shutdownTimeout;
    }

    public void setShutdownTimeoutUnit(TimeUnit shutdownTimeoutUnit) {
        _shutdownTimeoutUnit = shutdownTimeoutUnit;
    }

    public final <T> T createStub(Function<Channel, T> function) {
        return function.apply(_channel);
    }

    @Override
    public void close() {
        try {
            _channel.shutdown().awaitTermination(_shutdownTimeout, _shutdownTimeoutUnit);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted closing grpc channel {}", _channel.toString());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @param host
     * @param port
     * @return a non-secure standalone client
     */
    public static GrpcClient standalone(String host, int port) {
        return new GrpcClient(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    /**
     * @param name
     * @return a in-process client
     */
    public static GrpcClient standaloneInProcess(String name) {
        return new GrpcClient(InProcessChannelBuilder.forName(name).build());
    }

    /**
     * @param channel
     * @return a close-protected client on the given channel
     */
    public static GrpcClient shared(ManagedChannel channel) {
        return new GrpcClient(channel) {
            @Override
            public void close() {
                // close protected
            }
        };
    }

    // TODO checkout ManagedChannelProvider => flexible channel creation...
}
