package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorGrpcService;
import io.morethan.tweaky.test.GrpcServer;

public class ConductorTest {

    @Test
    void testNodeCount() throws Exception {
        GrpcServer grpcServer = new GrpcServer(0, new ConductorGrpcService());
        grpcServer.startAsync().awaitRunning();

        try (ConductorClient client = new ConductorClient("localhost", grpcServer.getPort())) {
            assertThat(client.nodeCount()).isEqualTo(23);
        }

    }
}
