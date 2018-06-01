package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class NodeRegistryTest {

    @Test
    void testNodeRegistration() {
        SingleTokenValidator tokenValidator = new SingleTokenValidator("my-cluster");
        NodeRegistry registry = new NodeRegistry(tokenValidator, new HostPortNameProvider());
        assertThat(registry.registeredNodes()).isEqualTo(0);

        // Register node with wrong token
        try {
            registry.registerNode("localhost", 23, "jerries-cluster");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            assertThat(e).hasMessageContaining("Invalid token");
        }
        assertThat(registry.registeredNodes()).isEqualTo(0);

        // Register 2 nodes successfully
        registry.registerNode("localhost", 23, "my-cluster");
        assertThat(registry.registeredNodes()).isEqualTo(1);
        registry.registerNode("localhost", 24, "my-cluster");
        assertThat(registry.registeredNodes()).isEqualTo(2);

        // Register already registered node again
        try {
            registry.registerNode("localhost", 24, "my-cluster");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            assertThat(e).hasMessageContaining("Name already given");
        }
    }

}
