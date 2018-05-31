package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.conductor.registration.SingleTokenValidator;
import io.morethan.tweaky.shared.GrpcClient;
import io.morethan.tweaky.test.GrpcServer;

public class NodeRegistryIntegrationTest {

    @Test
    void testNodeCount() throws Exception {
        String token = "abc123";
        GrpcServer grpcServer = new GrpcServer(0, new NodeRegistryGrpcService(new NodeRegistry(new SingleTokenValidator(token))));
        grpcServer.startAsync().awaitRunning();

        try (NodeRegistryClient client = new NodeRegistryClient(GrpcClient.standalone("localhost", grpcServer.getPort()))) {
            // succeed
            client.registerNode("localhost", 123, token);

            // fail
            try {
                client.registerNode("localhost", 123, "no-no-no");
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Invalid token").hasMessageContaining("no-no-no");
            }

            // succeed
            client.registerNode("localhost", 1234, token);
        }

    }
}
