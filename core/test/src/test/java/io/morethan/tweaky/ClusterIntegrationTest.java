package io.morethan.tweaky;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.common.util.concurrent.Service.State;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.noderegistry.NodeRegistryClient;
import io.morethan.tweaky.test.cluster.PlainMultiTokenCluster;
import io.morethan.tweaky.test.cluster.PlainSingleTokenCluster;
import io.morethan.tweaky.test.cluster.TestCluster;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class ClusterIntegrationTest {

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void testBootupAndRegistration_singleToken() throws Exception {
        TestCluster cluster = _shutdownHelper.register(new PlainSingleTokenCluster(3, ChannelProvider.plaintext(), "my-cluster") {
            @Override
            protected GrpcServer createNode(int number, int nodeRegistryPort, ChannelProvider channelProvider) {
                if (number == 2) {
                    return super.createNode(nodeRegistryPort, _token + "-invalid");
                }
                return super.createNode(number, nodeRegistryPort, channelProvider);
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

    @Test
    void testBootupAndRegistration_multiToken() throws Exception {
        TestCluster cluster = _shutdownHelper.register(new PlainMultiTokenCluster(3, ChannelProvider.plaintext()) {
            @Override
            protected GrpcServer createNode(int number, int nodeRegistryPort, ChannelProvider channelProvider) {
                if (number == 2) {
                    return super.createNode(nodeRegistryPort, "invalid-token");
                }
                return super.createNode(number, nodeRegistryPort, channelProvider);
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
