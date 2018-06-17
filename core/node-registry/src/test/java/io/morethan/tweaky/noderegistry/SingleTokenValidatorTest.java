package io.morethan.tweaky.noderegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.noderegistry.NodeContact;
import io.morethan.tweaky.noderegistry.SingleTokenValidator;
import io.morethan.tweaky.noderegistry.util.Try;

@SuppressWarnings("unchecked")
class SingleTokenValidatorTest {

    @Test
    void test() {
        SingleTokenValidator tokenValidator = new SingleTokenValidator("ABC");

        Supplier<Try<NodeContact, String>> successRegistration = mock(Supplier.class);
        when(successRegistration.get()).thenReturn(Try.success(mock(NodeContact.class)));

        assertThat(tokenValidator.accept("", 0, "", successRegistration).failure()).contains("Invalid token").doesNotContain("ABC");
        assertThat(tokenValidator.accept("", 0, "AFC", successRegistration).failure()).contains("Invalid token").contains("AFC").doesNotContain("ABC");
        assertThat(tokenValidator.accept("", 0, "ABC", successRegistration).isSuccess()).isTrue();
    }

}
