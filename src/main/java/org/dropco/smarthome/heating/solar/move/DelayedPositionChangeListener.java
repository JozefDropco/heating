package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class DelayedPositionChangeListener implements PositionChangeListener {
    private Consumer<AbsolutePosition> updater;
    private final int count;
    private AtomicInteger countDown = new AtomicInteger();
    private Lock lock = new ReentrantLock();

    public DelayedPositionChangeListener(int count, Consumer<AbsolutePosition> updater) {
        this.count = count;
        countDown.set(count);
        this.updater = updater;
    }

    public void onUpdate(AbsolutePosition position) {
        lock.lock();
        try {
            int current = countDown.decrementAndGet();
            if (current == 0) {
                countDown.set(count);
                updater.accept(position);
            }
        } finally {
            lock.unlock();
        }
    }
}
