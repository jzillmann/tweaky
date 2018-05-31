package io.morethan.tweaky;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.Conductor;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorGrpcService;
import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.conductor.registration.NodeTokenStore;
import io.morethan.tweaky.node.NodeClient;
import io.morethan.tweaky.node.NodeGrpcService;
import io.morethan.tweaky.testsupport.GrpcServerRule;

public class ClusterIntegrationTest {

    static final String VALID_TOKEN_1 = "node1";
    static final String VALID_TOKEN_2 = "node2";
    static final String INVALID_TOKEN = "humbug";

    NodeTokenStore _nodeTokenStore = new NodeTokenStore();
    NodeRegistry _nodeRegistry = new NodeRegistry(_nodeTokenStore);
    @RegisterExtension
    GrpcServerRule _conductorServer = new GrpcServerRule(new ConductorGrpcService(new Conductor(_nodeRegistry)), new NodeRegistryGrpcService(_nodeRegistry));
    @RegisterExtension
    GrpcServerRule _node1Server = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_1));
    @RegisterExtension
    GrpcServerRule _node2Server = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_2));
    @RegisterExtension
    GrpcServerRule _node3Server = new GrpcServerRule(new NodeGrpcService(INVALID_TOKEN));

    @Test
    void testBootCluster() throws Exception {
        _nodeTokenStore.addToken(VALID_TOKEN_1);
        _nodeTokenStore.addToken(VALID_TOKEN_2);
        try (ConductorClient conductorClient = new ConductorClient(_conductorServer.newStandaloneClient());
                NodeRegistryClient nodeRegistryClient = new NodeRegistryClient(_conductorServer.newStandaloneClient());
                NodeClient node1Client = new NodeClient(_node1Server.newStandaloneClient());
                NodeClient node2Client = new NodeClient(_node2Server.newStandaloneClient());
                NodeClient node3Client = new NodeClient(_node3Server.newStandaloneClient())) {

            assertThat(node1Client.token()).isEqualTo(VALID_TOKEN_1);
            assertThat(node2Client.token()).isEqualTo(VALID_TOKEN_2);
            assertThat(node3Client.token()).isEqualTo(INVALID_TOKEN);
            assertThat(conductorClient.nodeCount()).isEqualTo(0);

            nodeRegistryClient.registerNode("localhost", _node1Server.getPort(), VALID_TOKEN_1);
            assertThat(conductorClient.nodeCount()).isEqualTo(1);

            nodeRegistryClient.registerNode("localhost", _node2Server.getPort(), VALID_TOKEN_2);
            assertThat(conductorClient.nodeCount()).isEqualTo(2);

            try {
                nodeRegistryClient.registerNode("localhost", _node3Server.getPort(), INVALID_TOKEN);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Unknown token");
            }

            try {
                nodeRegistryClient.registerNode("localhost", _node3Server.getPort(), VALID_TOKEN_1);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("already used");
            }
            assertThat(conductorClient.nodeCount()).isEqualTo(2);
        }
    }
}
