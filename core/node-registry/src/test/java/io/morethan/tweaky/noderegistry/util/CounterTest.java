package io.morethan.tweaky.noderegistry.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.noderegistry.util.Counter;

class CounterTest {

    @Test
    void test() throws InterruptedException {
        Counter counter = new Counter();
        List<Exception> exceptions = new ArrayList<>();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    counter.awaitAtLeastCount(3);
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        };
        thread.start();
        thread.join(200);
        assertThat(thread.isAlive()).as("thread is alive").isTrue();
        counter.increment();
        counter.increment();
        assertThat(thread.isAlive()).as("thread is alive").isTrue();

        counter.increment();
        thread.join(200);
        assertThat(thread.isAlive()).as("thread is alive").isFalse();
        assertThat(exceptions).isEmpty();
    }

}
