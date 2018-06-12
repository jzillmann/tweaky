package io.morethan.tweaky.conductor.registration;

import io.grpc.Channel;

/**
 * Listener listening to node arrivals.
 */
public interface NodeListener {

    void addNode(NodeAddress nodeAddress, Channel channel);

}
