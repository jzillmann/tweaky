package io.morethan.tweaky;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.Conductor;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorGrpcService;
import io.morethan.tweaky.conductor.channel.DefaulftNodeChannelProvider;
import io.morethan.tweaky.conductor.channel.PlaintextChannelProvider;
import io.morethan.tweaky.conductor.registration.HostPortNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.conductor.registration.NodeRejectedException;
import io.morethan.tweaky.conductor.registration.NodeTokenStore;
import io.morethan.tweaky.conductor.registration.SingleTokenValidator;
import io.morethan.tweaky.node.Node;
import io.morethan.tweaky.testsupport.GrpcServerRule;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class ClusterIntegrationTest {

    static final String TOKEN = "my-cluster";

    NodeTokenStore _nodeTokenStore = new NodeTokenStore();
    NodeRegistry _nodeRegistry = new NodeRegistry(new SingleTokenValidator(TOKEN), new HostPortNameProvider(), new DefaulftNodeChannelProvider(new PlaintextChannelProvider()));

    @RegisterExtension
    GrpcServerRule _conductorServer = new GrpcServerRule(new ConductorGrpcService(new Conductor(_nodeRegistry)), new NodeRegistryGrpcService(_nodeRegistry));

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void testBootupAndRegistration() throws Exception {
        Node node1 = _shutdownHelper.register(new Node(0, TOKEN, "localhost", _conductorServer.getPort()));
        Node node2 = _shutdownHelper.register(new Node(0, TOKEN, "localhost", _conductorServer.getPort()));
        Node nodeWithInvalidToken = _shutdownHelper.register(new Node(0, TOKEN + "-invalid", "localhost", _conductorServer.getPort()));

        node1.startAsync();
        node2.startAsync();
        nodeWithInvalidToken.startAsync();

        try (ConductorClient conductorClient = new ConductorClient(_conductorServer.newStandaloneClient());) {

            Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
                int nodeCount = 0;
                do {
                    Thread.yield();
                    nodeCount = conductorClient.nodeCount();
                    System.out.println(nodeCount);
                } while (nodeCount < 2);
            });
        }
        assertThat(nodeWithInvalidToken.failureCause()).isNotNull().hasRootCauseInstanceOf(NodeRejectedException.class);
    }
}
