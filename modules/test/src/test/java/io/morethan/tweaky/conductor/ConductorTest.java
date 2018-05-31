package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.test.GrpcServer;

public class ConductorTest {

    @Test
    void testNodeCount() throws Exception {
        Conductor conductor = mock(Conductor.class);
        when(conductor.nodeCount()).thenReturn(23);

        GrpcServer grpcServer = new GrpcServer(0, new ConductorGrpcService(conductor));
        grpcServer.startAsync().awaitRunning();

        try (ConductorClient client = new ConductorClient("localhost", grpcServer.getPort())) {
            assertThat(client.nodeCount()).isEqualTo(23);
        }

    }
}
