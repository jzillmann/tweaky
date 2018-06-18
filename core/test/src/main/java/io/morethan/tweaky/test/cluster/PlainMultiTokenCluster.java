package io.morethan.tweaky.test.cluster;

import java.util.UUID;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeComponent;
import io.morethan.tweaky.noderegistry.NodeNameProvider;
import io.morethan.tweaky.noderegistry.NodeRegistrationValidator;
import io.morethan.tweaky.noderegistry.NodeRegistryComponent;
import io.morethan.tweaky.noderegistry.NodeTokenStore;

/**
 * A {@link TestCluster} which has no service deployed other than the core node & node-registry ones. Uses {@link NodeRegistrationValidator#tokenStore()} for membership control.
 */
public class PlainMultiTokenCluster extends TestCluster {

    private final String[] _nodeTokens;

    public PlainMultiTokenCluster(int numberOfNodes, ChannelProvider channelProvider) {
        super(numberOfNodes, channelProvider);
        _nodeTokens = new String[numberOfNodes];
        String tokenPrefix = UUID.randomUUID().toString();
        for (int i = 0; i < _nodeTokens.length; i++) {
            _nodeTokens[i] = tokenPrefix + "-" + i;
        }
    }

    @Override
    protected GrpcServer createNodeRegistry() {
        NodeTokenStore tokenStore = NodeRegistrationValidator.tokenStore();
        for (String token : _nodeTokens) {
            tokenStore.addToken(token);
        }
        return NodeRegistryComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(tokenStore)
                .build()
                .server();
    }

    @Override
    protected GrpcServer createNode(int number, int nodeRegistryPort) {
        return createNode(nodeRegistryPort, _nodeTokens[number]);
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
