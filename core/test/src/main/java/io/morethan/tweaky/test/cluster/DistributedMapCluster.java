package io.morethan.tweaky.test.cluster;

import io.morethan.examples.dm.gateway.GatewayComponent;
import io.morethan.tweaky.examples.dm.node.MapNodeComponent;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.noderegistry.NodeNameProvider;
import io.morethan.tweaky.noderegistry.NodeRegistrationValidator;

public class DistributedMapCluster extends TestCluster {

    private final String _clusterName;

    public DistributedMapCluster(int numberOfNodes, ChannelProvider channelProvider, String clusterName) {
        super(numberOfNodes, channelProvider);
        _clusterName = clusterName;
    }

    @Override
    protected GrpcServer createNodeRegistry() {
        return GatewayComponent.builder()
                .nodeCount(numberOfNodes())
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.singleToken(_clusterName))
                .build().server();
    }

    @Override
    protected GrpcServer createNode(int number, int nodeRegistryPort) {
        return MapNodeComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .token(_clusterName)
                .nodeRegistryHost("localhost")
                .nodeRegistryPort(nodeRegistryPort)
                .autoRegister(true)
                .build().server();
    }

}
