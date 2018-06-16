package io.morethan.tweaky.conductor.registration;

import io.grpc.Channel;
import io.morethan.tweaky.node.NodeClient;

/**
 * Provides a {@link NodeClient} based on a given {@link Channel}.
 */
public interface NodeClientProvider {

    NodeClient get(Channel channel);

    public static NodeClientProvider create() {
        return new NodeClientProvider() {
            @Override
            public NodeClient get(Channel channel) {
                return NodeClient.on(channel);
            }
        };
    }
}
