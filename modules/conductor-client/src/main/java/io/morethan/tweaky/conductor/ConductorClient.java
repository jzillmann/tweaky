package io.morethan.tweaky.conductor;

import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorBlockingStub;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorStub;
import io.morethan.tweaky.conductor.proto.ConductorProto.NodeCountRequest;
import io.morethan.tweaky.shared.Errors;
import io.morethan.tweaky.shared.GrpcClient;

/**
 * A client for {@link ConductorGrpc}.
 */
public class ConductorClient implements AutoCloseable {

    private final GrpcClient _grpcClient;
    private final ConductorBlockingStub _blockingStub;
    private final ConductorStub _asyncStub;

    public ConductorClient(GrpcClient grpcClient) {
        _grpcClient = grpcClient;
        _blockingStub = grpcClient.createStub(ConductorGrpc::newBlockingStub);
        _asyncStub = grpcClient.createStub(ConductorGrpc::newStub);
    }

    public int nodeCount() {
        try {
            return _blockingStub.nodeCount(NodeCountRequest.getDefaultInstance()).getCount();
        } catch (StatusRuntimeException e) {
            throw new RuntimeException(Errors.unwrap(e));
        }
    }

    @Override
    public void close() {
        _grpcClient.close();
    }
}
