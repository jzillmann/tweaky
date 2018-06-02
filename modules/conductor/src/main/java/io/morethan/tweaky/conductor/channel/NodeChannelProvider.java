package io.morethan.tweaky.conductor.channel;

/**
 * Creates a {@link NodeChannel} to a given host:port.
 */
public interface NodeChannelProvider {

    NodeChannel get(String host, int port);
}
