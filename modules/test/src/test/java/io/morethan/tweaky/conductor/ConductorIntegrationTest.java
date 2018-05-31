package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;

import io.morethan.tweaky.testsupport.GrpcServerRule;

@ExtendWith(MockitoExtension.class)
public class ConductorIntegrationTest {

    Conductor _conductor = mock(Conductor.class);
    @RegisterExtension
    GrpcServerRule _grpcServer = new GrpcServerRule(new ConductorGrpcService(_conductor));

    @Test
    void testNodeCount() throws Exception {
        when(_conductor.nodeCount()).thenReturn(23);
        try (ConductorClient client = new ConductorClient(_grpcServer.newStandaloneClient())) {
            assertThat(client.nodeCount()).isEqualTo(23);
        }

    }
}
