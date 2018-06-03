package io.morethan.tweaky.node;

import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.grpc.Errors;
import io.morethan.tweaky.grpc.GrpcClient;
import io.morethan.tweaky.node.proto.NodeGrpc;
import io.morethan.tweaky.node.proto.NodeGrpc.NodeBlockingStub;
import io.morethan.tweaky.node.proto.NodeProto.NodeTokenRequest;

/**
 * A client for {@link NodeGrpc}.
 */
public class NodeClient implements AutoCloseable {

    private final GrpcClient _grpcClient;
    private final NodeBlockingStub _blockingStub;

    public NodeClient(GrpcClient grpcClient) {
        _grpcClient = grpcClient;
        _blockingStub = grpcClient.createStub(NodeGrpc::newBlockingStub);
    }

    public String token() {
        try {
            return _blockingStub.token(NodeTokenRequest.getDefaultInstance()).getToken();
        } catch (StatusRuntimeException e) {
            throw Errors.unwrapped(e);
        }
    }

    @Override
    public void close() throws Exception {
        _grpcClient.close();
    }

}
