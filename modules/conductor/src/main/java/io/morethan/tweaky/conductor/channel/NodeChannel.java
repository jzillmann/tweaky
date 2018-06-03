package io.morethan.tweaky.conductor.channel;

import io.grpc.ManagedChannel;
import io.morethan.tweaky.grpc.GrpcClient;
import io.morethan.tweaky.node.NodeClient;

public class NodeChannel {

    private final ManagedChannel _managedChannel;

    public NodeChannel(ManagedChannel managedChannel) {
        _managedChannel = managedChannel;
    }

    public ManagedChannel getManagedChannel() {
        return _managedChannel;
    }

    public NodeClient open() {
        return new NodeClient(GrpcClient.shared(_managedChannel));
    }

}
