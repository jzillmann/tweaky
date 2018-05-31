package io.morethan.tweaky.conductor;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorBlockingStub;
import io.morethan.tweaky.conductor.proto.ConductorGrpc.ConductorStub;
import io.morethan.tweaky.conductor.proto.ConductorProto.NodeCountRequest;

/**
 * A client for {@link ConductorGrpcService}.
 */
public class ConductorClient implements AutoCloseable {

    private final ManagedChannel _rpcChannel;
    private final ConductorBlockingStub _blockingNodeProtocol;
    private final ConductorStub _asyncNodeProtocol;

    public ConductorClient(String host, int port) {
        _rpcChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        _blockingNodeProtocol = ConductorGrpc.newBlockingStub(_rpcChannel);
        _asyncNodeProtocol = ConductorGrpc.newStub(_rpcChannel);
    }

    public int nodeCount() {
        try {
            return _blockingNodeProtocol.nodeCount(NodeCountRequest.getDefaultInstance()).getCount();
        } catch (StatusRuntimeException e) {
            throw new RuntimeException(Errors.unwrap(e));
        }
    }

    @Override
    public void close() {
        try {
            _rpcChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO logging
            // LOG.warn("Interrupted closing node-client");
            Thread.currentThread().interrupt();
        }
    }
}
