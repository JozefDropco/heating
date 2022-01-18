package org.dropco.smarthome.heating.solar.move.vertical;

import org.dropco.smarthome.PinManager;
import org.dropco.smarthome.heating.solar.move.Movement;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerticalMove extends Thread {

    private AtomicReference<Movement> movement = new AtomicReference<>();
    private VerticalMoveFeedback feedback;
    private ReentrantLock lock = new ReentrantLock(true);
    private Condition waitForEnd = lock.newCondition();
    private final Logger LOGGER;
    private final PinManager pinManager;
    private AtomicInteger remaining = new AtomicInteger();
    private final Consumer<Integer> tickUpdater;
    private Consumer<Void> flush;
    BlockingQueue<Update> eventQueue = new LinkedBlockingQueue<>();
    AtomicBoolean running = new AtomicBoolean(true);

    private static final Semaphore update = new Semaphore(0);

    public VerticalMove(VerticalMoveFeedback feedback, Logger logger, PinManager pinManager, Consumer<Integer> tickUpdater, Consumer<Void> flush) {
        this.feedback = feedback;
        LOGGER = logger;
        this.pinManager = pinManager;
        this.tickUpdater = tickUpdater;
        this.flush = flush;
    }

    @Override
    public void run() {
        feedback.addRealTimeTicker(state -> {
            if (state) {
                eventQueue.add(Update.TICK);
                update.release();
            }
        });
        feedback.addMovingSubscriber(state -> {
            if (!state) {
                eventQueue.add(Update.STOP);
            }
        });
        while (running.get()) {
            try {
                Update upd = eventQueue.take();
                lock.lock();
                try {
                    if (upd == Update.STOP) {
                        Movement movement = this.movement.getAndSet(null);
                        if (movement != null) setState(movement, false);
                        flush.accept(null);
                        waitForEnd.signal();
                    }
                    if (upd == Update.TICK) {
                        int tick = movement.get().getTick();
                        tickUpdater.accept(tick);
                        if (remaining.addAndGet(-tick) == 0) {
                            setState(movement.get(), false);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Problem occurred", ex);
            }
        }
    }

    public void moveTo(Movement movement) {
        moveTo(movement, -movement.getTick() * 1000);
    }

    public void moveTo(Movement movement, int limit) {
        lock.lock();
        try {
            stopAndWait(movement.getShutdownFirst());
            this.movement.set(movement);
            remaining.set(limit);
            setState(movement, true);
        } finally {
            lock.unlock();
        }
    }

    public void stopMovement() {
        lock.lock();
        try {
            boolean stateChanged = setState(Movement.SOUTH, false);
            stateChanged |= setState(Movement.NORTH, false);
            if (stateChanged) waitForEnd();
        } finally {
            lock.unlock();
        }
    }

    private void stopAndWait(Movement movement) {
        lock.lock();
        try {
            boolean stateChanged = setState(movement, false);
            if (stateChanged) {
                waitForEnd();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Sleep interrupted");
        } finally {
            lock.unlock();
        }
    }

    public void waitForEnd() {
        lock.lock();
        try {
            if (feedback.isMoving()) {
                waitForEnd.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean setState(Movement movement, boolean state) {
        boolean pinState = pinManager.getState(movement.getPinRefCd()).isHigh();
        if (pinState != state) {
            if (state)
                LOGGER.log(Level.INFO, "Natáčam na " + movement.getName() + ".");
            else
                LOGGER.log(Level.INFO, "Zastavujem otáčanie na " + movement.getName() + ".");
            pinManager.setState(movement.getPinRefCd(), state);
            return true;
        }
        return false;
    }

    enum Update {
        TICK,
        STOP;
    }
}
