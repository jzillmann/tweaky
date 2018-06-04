package io.morethan.tweaky.grpc.client;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;

/**
 * A helper class for wrapping a {@link ManagedChannel} into a closable.
 */
public class ClosableChannel extends Channel implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ClosableChannel.class);

    private final ManagedChannel _channel;
    private long _shutdownTimeout = 5;
    private TimeUnit _shutdownTimeoutUnit = TimeUnit.SECONDS;

    private ClosableChannel(ManagedChannel channel) {
        _channel = channel;
    }

    public void withShutdownTimeout(long shutdownTimeout, TimeUnit shutdownTimeoutUnit) {
        _shutdownTimeout = shutdownTimeout;
        _shutdownTimeoutUnit = shutdownTimeoutUnit;
    }

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
        return _channel.newCall(methodDescriptor, callOptions);
    }

    @Override
    public String authority() {
        return _channel.authority();
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

    public static ClosableChannel of(ManagedChannel managedChannel) {
        return new ClosableChannel(managedChannel);
    }

}
