package io.morethan.tweaky;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.ConductorComponent;
import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.conductor.registration.NodeRejectedException;
import io.morethan.tweaky.grpc.GrpcServer;
import io.morethan.tweaky.grpc.GrpcServerModule;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.node.Node;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class ClusterIntegrationTest {

    static final String TOKEN = "my-cluster";

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void testBootupAndRegistration() throws Exception {
        GrpcServer conductorServer = _shutdownHelper.register(ConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.singleToken(TOKEN))
                .build()
                .conductorServer());
        conductorServer.startAsync().awaitRunning();

        Node node1 = _shutdownHelper.register(new Node(0, TOKEN, "localhost", conductorServer.getPort()));
        Node node2 = _shutdownHelper.register(new Node(0, TOKEN, "localhost", conductorServer.getPort()));
        Node nodeWithInvalidToken = _shutdownHelper.register(new Node(0, TOKEN + "-invalid", "localhost", conductorServer.getPort()));

        node1.startAsync();
        node2.startAsync();
        nodeWithInvalidToken.startAsync();

        try (ClosableChannel channel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", conductorServer.getPort()))) {
            NodeRegistryClient nodeRegistryClient = NodeRegistryClient.on(channel);
            Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
                int nodeCount = 0;
                do {
                    Thread.yield();
                    nodeCount = nodeRegistryClient.nodeCount();
                    System.out.println(nodeCount);
                } while (nodeCount < 2);
            });
        }

        try {
            nodeWithInvalidToken.awaitTerminated();
            fail("should throw exception");
        } catch (Exception e) {
            assertThat(e).hasRootCauseInstanceOf(NodeRejectedException.class);
        }
    }
}
