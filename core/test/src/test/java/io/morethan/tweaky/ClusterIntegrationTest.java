package io.morethan.tweaky;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.common.util.concurrent.Service.State;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeComponent;
import io.morethan.tweaky.noderegistry.NodeNameProvider;
import io.morethan.tweaky.noderegistry.NodeRegistrationValidator;
import io.morethan.tweaky.noderegistry.NodeRegistryClient;
import io.morethan.tweaky.noderegistry.NodeRegistryComponent;
import io.morethan.tweaky.test.TestCluster;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class ClusterIntegrationTest {

    static final String TOKEN = "my-cluster";

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void testBootupAndRegistration() throws Exception {
        TestCluster cluster = _shutdownHelper.register(new TestCluster(3, ChannelProvider.plaintext()) {

            @Override
            protected GrpcServer createNodeRegistry() {
                return NodeRegistryComponent.builder()
                        .grpcServerModule(GrpcServerModule.plaintext(0))
                        .nodeNameProvider(NodeNameProvider.hostPort())
                        .nodeRegistrationValidator(NodeRegistrationValidator.singleToken(TOKEN))
                        .build()
                        .server();
            }

            @Override
            protected GrpcServer createNode(int number, int nodeRegistryPort, ChannelProvider channelProvider) {
                String nodeToken = TOKEN;
                if (number == 2) {
                    nodeToken += "-invalid";
                }
                return NodeComponent.builder()
                        .grpcServerModule(GrpcServerModule.plaintext(0))
                        .token(nodeToken)
                        .nodeRegistryHost("localhost")
                        .nodeRegistryPort(nodeRegistryPort)
                        .autoRegister(true)
                        .build()
                        .server();
            }

        });
        cluster.boot().awaitNodes(2);
        cluster.nodes().get(2).awaitTerminated();
        assertThat(cluster.nodes().get(2).state()).isEqualTo(State.TERMINATED);
        // assertThat(e).hasRootCauseInstanceOf(NodeRejectedException.class);

        try (ClosableChannel channel = cluster.channelToNodeRegistry()) {
            NodeRegistryClient nodeRegistryClient = NodeRegistryClient.on(channel);
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(2);
            nodeRegistryClient.awaitNodes(1);
            nodeRegistryClient.awaitNodes(2);
        }

    }
}
