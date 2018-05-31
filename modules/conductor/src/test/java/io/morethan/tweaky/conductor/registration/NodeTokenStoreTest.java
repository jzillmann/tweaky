package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NodeTokenStoreTest {

    @Test
    void test() {
        NodeTokenStore nodeTokenStore = new NodeTokenStore();
        nodeTokenStore.addToken("tokenA");
        nodeTokenStore.addToken("tokenB");

        assertThat(nodeTokenStore.accept("", 0, "tokenA")).isEmpty();
        assertThat(nodeTokenStore.accept("", 0, "tokenA")).isPresent().get().asString().contains("already used").contains("tokenA");

        assertThat(nodeTokenStore.accept("", 0, "tokenB")).isEmpty();
        assertThat(nodeTokenStore.accept("", 0, "tokenB")).isPresent().get().asString().contains("already used").contains("tokenB");

        assertThat(nodeTokenStore.accept("", 0, "tokenC")).isPresent().get().asString().contains("Unknown token").contains("tokenC");
    }

}
