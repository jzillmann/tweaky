package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.channel.DefaulftNodeChannelProvider;
import io.morethan.tweaky.conductor.channel.PlaintextChannelProvider;
import io.morethan.tweaky.conductor.registration.HostPortNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.conductor.registration.NodeTokenStore;
import io.morethan.tweaky.node.NodeClient;
import io.morethan.tweaky.node.NodeGrpcService;
import io.morethan.tweaky.testsupport.GrpcServerRule;

public class NodeRegistryIntegrationTest {

    static final String VALID_TOKEN_1 = "node1";
    static final String VALID_TOKEN_2 = "node2";
    static final String VALID_TOKEN_3 = "node3";
    static final String INVALID_TOKEN = "humbug";

    NodeTokenStore _nodeTokenStore = new NodeTokenStore();
    NodeRegistry _nodeRegistry = new NodeRegistry(_nodeTokenStore, new HostPortNameProvider(), new DefaulftNodeChannelProvider(new PlaintextChannelProvider()));
    @RegisterExtension
    GrpcServerRule _conductor = new GrpcServerRule(new ConductorGrpcService(new Conductor(_nodeRegistry)), new NodeRegistryGrpcService(_nodeRegistry));
    @RegisterExtension
    GrpcServerRule _node1Server = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_1));
    @RegisterExtension
    GrpcServerRule _node2Server = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_2));
    @RegisterExtension
    GrpcServerRule _node3InvalidServer = new GrpcServerRule(new NodeGrpcService(INVALID_TOKEN));
    @RegisterExtension
    GrpcServerRule _node3ValidServer = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_3));

    @Test
    void testNodeRegistration() throws Exception {
        _nodeTokenStore.addToken(VALID_TOKEN_1);
        _nodeTokenStore.addToken(VALID_TOKEN_2);
        _nodeTokenStore.addToken(VALID_TOKEN_3);
        try (ConductorClient conductorClient = new ConductorClient(_conductor.newStandaloneClient());
                NodeRegistryClient nodeRegistryClient = new NodeRegistryClient(_conductor.newStandaloneClient());
                NodeClient node1Client = new NodeClient(_node1Server.newStandaloneClient());
                NodeClient node2Client = new NodeClient(_node2Server.newStandaloneClient());
                NodeClient node3InvalidClient = new NodeClient(_node3InvalidServer.newStandaloneClient());
                NodeClient node3ValidClient = new NodeClient(_node3ValidServer.newStandaloneClient());) {

            assertThat(node1Client.token()).isEqualTo(VALID_TOKEN_1);
            assertThat(node2Client.token()).isEqualTo(VALID_TOKEN_2);
            assertThat(node3InvalidClient.token()).isEqualTo(INVALID_TOKEN);
            assertThat(node3ValidClient.token()).isEqualTo(VALID_TOKEN_3);
            assertThat(conductorClient.nodeCount()).isEqualTo(0);

            // 2 successful nodes
            nodeRegistryClient.registerNode("localhost", _node1Server.getPort(), VALID_TOKEN_1);
            assertThat(conductorClient.nodeCount()).isEqualTo(1);

            nodeRegistryClient.registerNode("localhost", _node2Server.getPort(), VALID_TOKEN_2);
            assertThat(conductorClient.nodeCount()).isEqualTo(2);

            // node with wrong token
            try {
                nodeRegistryClient.registerNode("localhost", _node3InvalidServer.getPort(), INVALID_TOKEN);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Unknown token");
            }

            // register node twice
            try {
                nodeRegistryClient.registerNode("localhost", _node3InvalidServer.getPort(), VALID_TOKEN_1);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("already used");
            }
            assertThat(conductorClient.nodeCount()).isEqualTo(2);

            // register node with not-matching remote token
            try {
                nodeRegistryClient.registerNode("localhost", _node3InvalidServer.getPort(), VALID_TOKEN_3);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Remote token does not match");
            }
            assertThat(conductorClient.nodeCount()).isEqualTo(2);

            // register node which has no server running
            try {
                nodeRegistryClient.registerNode("localhost", 23, VALID_TOKEN_3);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Could not establish channel").hasMessageContaining("localhost:23");
            }
            assertThat(conductorClient.nodeCount()).isEqualTo(2);

            // register valid node3
            nodeRegistryClient.registerNode("localhost", _node3ValidServer.getPort(), VALID_TOKEN_3);
            assertThat(conductorClient.nodeCount()).isEqualTo(3);
        }

    }
}
