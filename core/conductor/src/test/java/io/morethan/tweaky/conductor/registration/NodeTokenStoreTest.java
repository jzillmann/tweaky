package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.morethan.tweaky.conductor.util.Try;

@SuppressWarnings("unchecked")
class NodeTokenStoreTest {

    private static final Logger LOG = LoggerFactory.getLogger(NodeTokenStoreTest.class);

    @Test
    void test() {
        NodeTokenStore tokenStore = new NodeTokenStore();
        tokenStore.addToken("tokenA");
        tokenStore.addToken("tokenB");

        Supplier<Try<NodeContact, String>> successRegistration = mock(Supplier.class);
        when(successRegistration.get()).thenReturn(Try.success(mock(NodeContact.class)));

        assertThat(tokenStore.accept("", 0, "tokenA", successRegistration).isSuccess()).isTrue();
        assertThat(tokenStore.accept("", 0, "tokenA", successRegistration).failure()).contains("already used").contains("tokenA");

        assertThat(tokenStore.accept("", 0, "tokenB", successRegistration).isSuccess()).isTrue();
        assertThat(tokenStore.accept("", 0, "tokenB", successRegistration).failure()).contains("already used").contains("tokenB");

        assertThat(tokenStore.accept("", 0, "tokenC", successRegistration).failure()).contains("Unknown token").contains("tokenC");
    }

    @Test
    void testKeepTokenOnFailure() {
        NodeTokenStore tokenStore = new NodeTokenStore();
        tokenStore.addToken("tokenA");

        Supplier<Try<NodeContact, String>> successRegistration = mock(Supplier.class);
        when(successRegistration.get()).thenReturn(Try.success(mock(NodeContact.class)));

        Supplier<Try<NodeContact, String>> failureRegistration = mock(Supplier.class);
        when(failureRegistration.get()).thenReturn(Try.failure("failed for some reason"));

        Try<NodeContact, String> failureTry = tokenStore.accept("", 0, "tokenA", failureRegistration);
        assertThat(failureTry.isSuccess()).isFalse();
        assertThat(failureTry.failure()).isEqualTo("failed for some reason");

        Try<NodeContact, String> successTry = tokenStore.accept("", 0, "tokenA", successRegistration);
        assertThat(successTry.isSuccess()).isTrue();
    }

    @Test
    @RepeatedTest(5)
    void testConcurrent() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            String[] tokens = new String[50];
            NodeTokenStore tokenStore = new NodeTokenStore();
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = "token" + i;
                tokenStore.addToken(tokens[i]);
            }

            CountDownLatch allTokensRegistered = new CountDownLatch(tokens.length);
            Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
            Set<String> registrationFailures = ConcurrentHashMap.newKeySet();
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread() {
                    @Override
                    public void run() {
                        Random random = new Random();
                        Try<NodeContact, String> registrationResult;
                        if (random.nextInt(10) < 3) {
                            registrationResult = Try.failure("failed for some reason");
                        } else {
                            registrationResult = Try.success(mock(NodeContact.class));
                        }
                        while (allTokensRegistered.getCount() > 0) {
                            String token = tokens[random.nextInt(tokens.length)];
                            Try<NodeContact, String> registrationTry = tokenStore.accept("localhost", random.nextInt(20_000), token, () -> registrationResult);
                            if (registrationTry.isSuccess()) {
                                allTokensRegistered.countDown();
                            } else {
                                registrationFailures.add(registrationTry.failure());
                            }
                            String resultString = registrationTry.isSuccess() ? "success!" : registrationTry.failure();
                            LOG.info("Tried register token {} - {}", token, resultString);
                        }
                    }
                };
                threads[i].start();
            }

            allTokensRegistered.await();
            assertThat(registrationFailures).isNotEmpty();
            for (String failure : registrationFailures) {
                if (!failure.contains("failed for some reason") && !failure.contains("Token already used")) {
                    fail("Unexpected failure: " + failure);
                }
            }
        });
    }

}
