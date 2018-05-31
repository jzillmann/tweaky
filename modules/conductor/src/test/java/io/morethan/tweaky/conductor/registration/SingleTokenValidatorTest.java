package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SingleTokenValidatorTest {

    @Test
    void test() {
        SingleTokenValidator tokenValidator = new SingleTokenValidator("ABC");

        assertThat(tokenValidator.accept("", 0, "")).isPresent().get().asString().contains("Invalid token").doesNotContain("ABC");
        assertThat(tokenValidator.accept("", 0, "AFC")).isPresent().get().asString().contains("Invalid token").contains("AFC").doesNotContain("ABC");
        assertThat(tokenValidator.accept("", 0, "ABC")).isEmpty();
    }

}
