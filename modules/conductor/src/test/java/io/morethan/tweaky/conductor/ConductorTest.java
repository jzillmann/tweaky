package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.conductor.registration.NodeRegistry;

class ConductorTest {

    @Test
    void testNodeRegistration() {
        NodeRegistry nodeRegistry = mock(NodeRegistry.class);
        Conductor conductor = new Conductor(nodeRegistry);
        assertThat(conductor.nodeCount()).isEqualTo(0);

        when(nodeRegistry.registeredNodes()).thenReturn(23);
        assertThat(conductor.nodeCount()).isEqualTo(23);
    }

}
