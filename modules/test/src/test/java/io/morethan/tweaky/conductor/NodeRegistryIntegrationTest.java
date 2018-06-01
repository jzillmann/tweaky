package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.registration.HostPortNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.conductor.registration.NodeRegistryGrpcService;
import io.morethan.tweaky.conductor.registration.SingleTokenValidator;
import io.morethan.tweaky.testsupport.GrpcServerRule;

public class NodeRegistryIntegrationTest {

    private static final String TOKEN = "abc123";

    @RegisterExtension
    static GrpcServerRule _grpcServer = new GrpcServerRule(new NodeRegistryGrpcService(new NodeRegistry(new SingleTokenValidator(TOKEN), new HostPortNameProvider())));

    @Test
    void testNodeCount() throws Exception {
        try (NodeRegistryClient client = new NodeRegistryClient(_grpcServer.newStandaloneClient())) {
            // succeed
            client.registerNode("localhost", 123, TOKEN);

            // fail cause already registered
            try {
                client.registerNode("localhost", 123, TOKEN);
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Name already given");
            }

            // fail cause invalid token
            try {
                client.registerNode("localhost", 123, "no-no-no");
                fail("should throw exception");
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("Invalid token").hasMessageContaining("no-no-no");
            }

            // succeed
            client.registerNode("localhost", 1234, TOKEN);
        }

    }
}
