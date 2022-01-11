package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Sets;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.PinManager;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.Position;
import org.dropco.smarthome.heating.solar.dto.PositionProcessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static org.dropco.smarthome.heating.solar.SolarSystemRefCode.*;

public class SolarPanelMover implements Mover {

    private static final Logger LOGGER = Logger.getLogger(SolarPanelMover.class.getName());
    private Supplier<AbsolutePosition> currentPositionSupplier;
    private PinManager pinManager;

    private Consumer<Void> flushPosition;
    private List<PositionChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private AtomicReference<String> lastMovementRefCd = new AtomicReference<>();
    private AtomicReference<Movement> horizontalMovement = new AtomicReference<>();
    private AtomicReference<Movement> verticalMovement = new AtomicReference<>();
    private AtomicReference<PosDiff> remainingDiff = new AtomicReference<>();
    private VerticalMoveFeedback verticalMoveFeedback;
    private HorizontalMoveFeedback horizontalMoveFeedback;
    private ReentrantLock lock = new ReentrantLock(true);
    private Condition waitForEnd = lock.newCondition();

    public SolarPanelMover(PinManager pinManager, Supplier<AbsolutePosition> currentPositionSupplier, Consumer<Void> flushPosition, VerticalMoveFeedback verticalMoveFeedback, HorizontalMoveFeedback horizontalMoveFeedback) {
        this.pinManager = pinManager;
        this.currentPositionSupplier = currentPositionSupplier;
        this.flushPosition = flushPosition;
        this.verticalMoveFeedback = verticalMoveFeedback;
        this.horizontalMoveFeedback = horizontalMoveFeedback;
    }


    public void connect() {
        verticalMoveFeedback.addRealTimeTicker(state -> {
            lock.lock();
            try {
                Movement movement = verticalMovement.get();
                if (state && movement != null) {
                    AbsolutePosition currentPosition = currentPositionSupplier.get();
                    currentPosition.setVertical(currentPosition.getVertical() + movement.tick);
                    if (remainingDiff.get().decVert(movement.tick))
                        setState(movement, false);
                    fireUpdate(currentPosition);
                }
            } finally {
                lock.unlock();
            }
        });
        verticalMoveFeedback.addSubscriber(state -> {
            lock.lock();
            try {
                if (!state) {
                    Movement movement = verticalMovement.getAndSet(null);
                    if (movement != null) setState(movement, false);
                    if (horizontalMovement.get() == null) {
                        waitForEnd.signal();
                        flushPosition.accept(null);
                    }
                }

            } finally {
                lock.unlock();
            }
        });
        horizontalMoveFeedback.addRealTimeTicker(state -> {
            lock.lock();
            try {
                Movement movement = horizontalMovement.get();
                if (state && movement != null) {
                    AbsolutePosition currentPosition = currentPositionSupplier.get();
                    currentPosition.setHorizontal(currentPosition.getHorizontal() + movement.tick);
                    if (remainingDiff.get().decHor(movement.tick))
                        setState(movement, false);
                    fireUpdate(currentPosition);
                }
            } finally {
                lock.unlock();
            }
        });
        horizontalMoveFeedback.addSubscriber(state -> {
            lock.lock();
            try {
                if (!state) {
                    Movement movement = horizontalMovement.getAndSet(null);
                    if (movement != null) setState(movement, false);
                    if (verticalMovement.get() == null) {
                        flushPosition.accept(null);
                        waitForEnd.signal();
                    }
                }

            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public synchronized void moveTo(String movementRefCd, Position position) {
        if (ServiceMode.isServiceMode()) return;
        lock.lock();
        try {
            if (Objects.equals(lastMovementRefCd.get(), movementRefCd)) return;
            lastMovementRefCd.set(movementRefCd);
            PosDiff diff = calculateDifference(position, currentPositionSupplier.get());
            if (diff.getHor() == 0 && diff.getVert() == 0) return;
            stop();
            remainingDiff.set(diff);
            int absHorizontal = abs(diff.getHor());
            LOGGER.fine("Posun o [hor=" + diff.getHor() + ", vert=" + diff.getVert() + "]");
            if (absHorizontal > 0) {
                Movement horMovement = getHorMovement(diff);
                horizontalMovement.set(horMovement);
                setState(horMovement, true);
            }
            int absVertical = abs(diff.getVert());
            if (absVertical > 0) {
                Movement vertMovement = getVertMovement(diff);
                verticalMovement.set(vertMovement);
                setState(vertMovement, true);
            }
        } finally {
            lock.unlock();
        }
        // WAIT for second if feedback didnt came back stop() it
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Spanok preruseny");
        }
        if (!Thread.interrupted()) {
            lock.lock();
            try {
                PosDiff posDiff = remainingDiff.get();
                if (posDiff != null) {
                    if ((posDiff.getHor() > 0 && !horizontalMoveFeedback.getMoving()) || (posDiff.getVert() > 0 && !verticalMoveFeedback.getMoving()))
                        stop();
                }
            } finally {
                lock.unlock();
            }
        }

    }

    public void stop() {
        lock.lock();
        try {
            setState(Movement.WEST, false);
            setState(Movement.EAST, false);
            setState(Movement.NORTH, false);
            setState(Movement.SOUTH, false);
            waitForEnd();
        } finally {
            lock.unlock();
        }
    }

    public void waitForEnd() {
        lock.lock();
        try {
            if (verticalMoveFeedback.getMoving() || horizontalMoveFeedback.getMoving()) {
                waitForEnd.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }


    private PosDiff calculateDifference(Position position, final AbsolutePosition previousPosition) {
        PosDiff diff = position.invoke(new PositionProcessor<PosDiff>() {
            @Override
            public PosDiff process(AbsolutePosition absPos) {
                LOGGER.log(Level.FINE, "Natáčanie kolektorov na hor=" + absPos.getHorizontal() + ", vert=" + absPos.getVertical());
                return new PosDiff()
                        .setHor(absPos.getHorizontal() - previousPosition.getHorizontal())
                        .setVert(absPos.getVertical() - previousPosition.getVertical());
            }

            @Override
            public PosDiff process(DeltaPosition deltaPos) {
                LOGGER.log(Level.FINE, "Natáčanie kolektorov o hor=" + deltaPos.getDeltaHorizontalTicks() + ", vert=" + deltaPos.getDeltaVerticalTicks());
                return new PosDiff()
                        .setHor(deltaPos.getDeltaHorizontalTicks())
                        .setVert(deltaPos.getDeltaVerticalTicks());
            }
        });
        return diff;
    }

    private Movement getVertMovement(PosDiff diff) {
        if (diff.getVert() < 0) return Movement.NORTH;
        return Movement.SOUTH;
    }

    private Movement getHorMovement(PosDiff diff) {
        if (diff.getHor() < 0) return Movement.WEST;
        return Movement.EAST;
    }


    public void setState(Movement movement, boolean state) {
        lock.lock();
        try {
            PinState pinState = pinManager.getState(movement.pinRefCd);
            if (pinState.isHigh() != state) {
                if (state)
                    LOGGER.log(Level.INFO, "Natáčam na " + movement.name + ".");
                else
                    LOGGER.log(Level.INFO, "Zastavujem otáčanie na " + movement.name + ".");
                pinManager.setState(movement.pinRefCd, state);
            }
        } finally {
            lock.unlock();
        }
    }

    private void fireUpdate(AbsolutePosition currentPosition) {
        for (PositionChangeListener listener : listeners)
            listener.onUpdate(currentPosition);
    }

    public void addListener(PositionChangeListener listener) {
        listeners.add(listener);
    }



    public enum Movement {
        SOUTH(SOUTH_PIN_REF_CD, 1, "Juh"),
        NORTH(NORTH_PIN_REF_CD, -1, "Sever"),
        WEST(WEST_PIN_REF_CD, -1, "Západ"),
        EAST(EAST_PIN_REF_CD, 1, "Východ");

        private final String pinRefCd;
        private int tick;
        private final String name;


        Movement(String pinRefCd, int tick, String name) {
            this.pinRefCd = pinRefCd;
            this.tick = tick;
            this.name = name;
        }

        /***
         * Gets the pinRefCd
         * @return
         */
        public String getPinRefCd() {
            return pinRefCd;
        }
    }


}
