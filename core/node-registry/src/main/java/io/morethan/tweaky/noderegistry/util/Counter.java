package io.morethan.tweaky.noderegistry.util;

/**
 * Simple counter on {@link Integer} which allows waiting for a certain count to be reached.
 */
public class Counter {

    private int _count;

    public void increment() {
        synchronized (this) {
            _count++;
            this.notifyAll();
        }
    }

    public void awaitAtLeastCount(int expectedCount) throws InterruptedException {
        while (_count < expectedCount) {
            synchronized (this) {
                if (_count >= expectedCount) {
                    return;
                }
                this.wait();
            }
        }
    }
}
