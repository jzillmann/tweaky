package io.morethan.tweaky.test.cluster;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeComponent;
import io.morethan.tweaky.noderegistry.NodeNameProvider;
import io.morethan.tweaky.noderegistry.NodeRegistrationValidator;
import io.morethan.tweaky.noderegistry.NodeRegistryComponent;

/**
 * A {@link TestCluster} which has no service deployed other than the core node & node-registry ones. Uses {@link NodeRegistrationValidator#singleToken(String)} for membership
 * control.
 */
public class PlainSingleTokenCluster extends TestCluster {

    protected final String _token;

    public PlainSingleTokenCluster(int numberOfNodes, ChannelProvider channelProvider, String token) {
        super(numberOfNodes, channelProvider);
        _token = token;
    }

    @Override
    protected GrpcServer createNodeRegistry() {
        return NodeRegistryComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.singleToken(_token))
                .build()
                .server();
    }

    @Override
    protected GrpcServer createNode(int number, int nodeRegistryPort) {
        return createNode(nodeRegistryPort, _token);
    }

    protected GrpcServer createNode(int nodeRegistryPort, String nodeToken) {
        return NodeComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .token(nodeToken)
                .nodeRegistryHost("localhost")
                .nodeRegistryPort(nodeRegistryPort)
                .autoRegister(true)
                .build()
                .server();
    }

}
