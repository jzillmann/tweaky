package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.conductor.registration.SingleTokenValidator;

class ConductorTest {

    @Test
    void testNodeRegistration() {
        SingleTokenValidator tokenValidator = new SingleTokenValidator("abc123");
        Conductor conductor = new Conductor(tokenValidator);
        assertThat(conductor.nodeCount()).isEqualTo(0);
        try {
            conductor.registerNode("localhost", 23, "laLeLu");
            fail("should throw exception");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Rejected node");
            assertThat(conductor.nodeCount()).isEqualTo(0);
        }

        conductor.registerNode("localhost", 23, "abc123");
        assertThat(conductor.nodeCount()).isEqualTo(1);
        conductor.registerNode("localhost", 24, "abc123");
        assertThat(conductor.nodeCount()).isEqualTo(2);
    }

}
