package io.morethan.tweaky.node;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.node.proto.NodeGrpc;
import io.morethan.tweaky.node.proto.NodeGrpc.NodeBlockingStub;
import io.morethan.tweaky.node.proto.NodeProto.NodeTokenRequest;

/**
 * A client for {@link NodeGrpc}.
 */
public class NodeClient {

    private final NodeBlockingStub _blockingStub;

    private NodeClient(NodeBlockingStub blockingStub) {
        _blockingStub = blockingStub;
    }

    public String token() {
        try {
            return _blockingStub.token(NodeTokenRequest.getDefaultInstance()).getToken();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    public static NodeClient on(Channel channel) {
        return new NodeClient(NodeGrpc.newBlockingStub(channel));
    }

}
