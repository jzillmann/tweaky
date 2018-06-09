package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dagger.Lazy;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.conductor.registration.NodeTokenStore;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeClient;
import io.morethan.tweaky.node.NodeGrpcService;
import io.morethan.tweaky.testsupport.GrpcServerRule;
import io.morethan.tweaky.testsupport.ShutdownHelper;

@SuppressWarnings("unchecked")
public class NodeRegistryIntegrationTest {

    static final String VALID_TOKEN_1 = "node1";
    static final String VALID_TOKEN_2 = "node2";
    static final String VALID_TOKEN_3 = "node3";
    static final String INVALID_TOKEN = "humbug";

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();
    @RegisterExtension
    GrpcServerRule _node1Server = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_1, mock(Lazy.class)));
    @RegisterExtension
    GrpcServerRule _node2Server = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_2, mock(Lazy.class)));
    @RegisterExtension
    GrpcServerRule _node3InvalidServer = new GrpcServerRule(new NodeGrpcService(INVALID_TOKEN, mock(Lazy.class)));
    @RegisterExtension
    GrpcServerRule _node3ValidServer = new GrpcServerRule(new NodeGrpcService(VALID_TOKEN_3, mock(Lazy.class)));

    @Test
    void testTokenStore() throws Exception {
        NodeTokenStore tokenStore = NodeRegistrationValidator.tokenStore();
        tokenStore.addToken(VALID_TOKEN_1);
        tokenStore.addToken(VALID_TOKEN_2);
        tokenStore.addToken(VALID_TOKEN_3);

        GrpcServer conductorServer = _shutdownHelper.register(
                ConductorComponent.builder()
                        .grpcServerModule(GrpcServerModule.plaintext(0))
                        .nodeNameProvider(NodeNameProvider.hostPort())
                        .nodeRegistrationValidator(tokenStore)
                        .build()
                        .server());
        conductorServer.startAsync().awaitRunning();

        try (ClosableChannel conductorChannel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", conductorServer.getPort()))) {
            NodeRegistryClient nodeRegistryClient = NodeRegistryClient.on(conductorChannel);
            NodeClient node1Client = NodeClient.on(_node1Server.newChannel());
            NodeClient node2Client = NodeClient.on(_node2Server.newChannel());
            NodeClient node3InvalidClient = NodeClient.on(_node3InvalidServer.newChannel());
            NodeClient node3ValidClient = NodeClient.on(_node3ValidServer.newChannel());

            assertThat(node1Client.token()).isEqualTo(VALID_TOKEN_1);
            assertThat(node2Client.token()).isEqualTo(VALID_TOKEN_2);
            assertThat(node3InvalidClient.token()).isEqualTo(INVALID_TOKEN);
            assertThat(node3ValidClient.token()).isEqualTo(VALID_TOKEN_3);
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(0);

            // 2 successful nodes
            nodeRegistryClient.registerNode("localhost", _node1Server.getPort(), VALID_TOKEN_1);
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(1);

            nodeRegistryClient.registerNode("localhost", _node2Server.getPort(), VALID_TOKEN_2);
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(2);

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
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(2);

            // register node with not-matching remote token
            try {
                nodeRegistryClient.registerNode("localhost", _node3InvalidServer.getPort(), VALID_TOKEN_3);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Remote token does not match");
            }
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(2);

            // register node which has no server running
            try {
                nodeRegistryClient.registerNode("localhost", 23, VALID_TOKEN_3);
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Could not establish channel").hasMessageContaining("localhost:23");
            }
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(2);

            // register valid node3
            nodeRegistryClient.registerNode("localhost", _node3ValidServer.getPort(), VALID_TOKEN_3);
            assertThat(nodeRegistryClient.nodeCount()).isEqualTo(3);
        }

    }
}
