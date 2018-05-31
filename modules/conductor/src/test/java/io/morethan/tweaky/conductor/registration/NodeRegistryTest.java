package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class NodeRegistryTest {

    @Test
    void testNodeRegistration() {
        SingleTokenValidator tokenValidator = new SingleTokenValidator("abc123");
        NodeRegistry registry = new NodeRegistry(tokenValidator);
        assertThat(registry.registeredNodes()).isEqualTo(0);
        try {
            registry.registerNode("localhost", 23, "laLeLu");
            fail("should throw exception");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Rejected node");
            assertThat(registry.registeredNodes()).isEqualTo(0);
        }

        registry.registerNode("localhost", 23, "abc123");
        assertThat(registry.registeredNodes()).isEqualTo(1);
        registry.registerNode("localhost", 24, "abc123");
        assertThat(registry.registeredNodes()).isEqualTo(2);
    }

}
