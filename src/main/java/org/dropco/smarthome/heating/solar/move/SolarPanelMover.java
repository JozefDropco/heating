package org.dropco.smarthome.heating.solar.move;

import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.PinManager;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.Position;
import org.dropco.smarthome.heating.solar.dto.PositionProcessor;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static org.dropco.smarthome.heating.solar.SolarSystemRefCode.*;

public class SolarPanelMover implements Mover, Runnable {

    private static final Semaphore waitForEnd = new Semaphore(0);
    private static final Logger LOGGER = Logger.getLogger(SolarPanelMover.class.getName());
    private Supplier<AbsolutePosition> currentPositionSupplier;
    private PinManager pinManager;
    private List<PositionChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private BlockingQueue<MoveEvent> moveEvents = new ArrayBlockingQueue<>(100);
    private AtomicReference<String> lastMovementRefCd = new AtomicReference<>();
    private AtomicReference<Movement> horizontalMovement = new AtomicReference<>();
    private AtomicReference<Movement> verticalMovement = new AtomicReference<>();
    private AtomicReference<PosDiff> remainingDiff = new AtomicReference<>();
    private VerticalMoveFeedback verticalMoveFeedback;
    private HorizontalMoveFeedback horizontalMoveFeedback;

    public SolarPanelMover(PinManager pinManager, Supplier<AbsolutePosition> currentPositionSupplier, VerticalMoveFeedback verticalMoveFeedback, HorizontalMoveFeedback horizontalMoveFeedback) {
        this.pinManager = pinManager;
        this.currentPositionSupplier = currentPositionSupplier;
        this.verticalMoveFeedback = verticalMoveFeedback;
        this.horizontalMoveFeedback = horizontalMoveFeedback;
    }


    @Override
    public synchronized void moveTo(String movementRefCd, Position position) {
        if (Objects.equals(lastMovementRefCd.get(),movementRefCd)) return;
        lastMovementRefCd.set(movementRefCd);
        PosDiff diff = calculateDifference(position, currentPositionSupplier.get());
        if (diff.vert==0 && diff.hor==0) return;
        stop();
        int absHorizontal = abs(diff.hor);
        LOGGER.fine("Posun o [hor=" + diff.hor + ", vert=" + diff.vert + "]");
        if (absHorizontal > 0) {
            Movement horMovement = getHorMovement(diff);
            horizontalMovement.set(horMovement);
            setState(horMovement.shutdownFirst, false);
            setState(horMovement, true);
        }
        int absVertical = abs(diff.vert);
        if (absVertical > 0) {
            Movement vertMovement = getVertMovement(diff);
            verticalMovement.set(vertMovement);
            setState(vertMovement.shutdownFirst, false);
            setState(vertMovement, true);
        }
    }

    public void stop() {
        if (remainingDiff.get() != null) {
            if (horizontalMovement.get() != null)
                setState(horizontalMovement.get(), false);
            if (verticalMovement.get() != null)
                setState(verticalMovement.get(), false);
            waitForEnd.acquireUninterruptibly();
        }
    }


    public void run() {
        verticalMoveFeedback.addRealTimeTicker(state -> {
            if (state) moveEvents.add(new MoveEvent(verticalMovement.get(), EventType.TICK));
        });
        verticalMoveFeedback.addSubscriber(state -> {
            if (!state) moveEvents.add(new MoveEvent(verticalMovement.get(), EventType.STOP));
        });
        horizontalMoveFeedback.addRealTimeTicker(state -> {
            if (state) moveEvents.add(new MoveEvent(horizontalMovement.get(), EventType.TICK));
        });
        horizontalMoveFeedback.addSubscriber(state -> {
            if (!state) moveEvents.add(new MoveEvent(horizontalMovement.get(), EventType.STOP));
        });
        try {
            MoveEvent e;
            Optional<AbsolutePosition> position = Optional.empty();
            while ((e = moveEvents.take()) != null) {
                AbsolutePosition currentPosition = position.orElseGet(currentPositionSupplier);
                position =Optional.of(currentPosition);
                switch (e.eventType) {
                    case TICK:
                        switch (e.movement) {
                            case WEST:
                            case EAST:
                                currentPosition.setHorizontal(currentPosition.getHorizontal() + e.movement.tick);
                                break;
                            case NORTH:
                            case SOUTH:
                                currentPosition.setVertical(currentPosition.getVertical() + e.movement.tick);
                                break;
                        }
                        break;
                    case STOP:
                        switch (e.movement) {
                            case WEST:
                            case EAST:
                                horizontalMovement.set(null);
                                break;
                            case NORTH:
                            case SOUTH:
                                verticalMovement.set(null);
                                break;
                        }
                        break;
                }
                fireUpdate(currentPosition);
                if (verticalMovement.get()==null && horizontalMovement.get() ==null){
                    if (waitForEnd.hasQueuedThreads()) waitForEnd.release();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.FINE,"Solar panel preruseny", e  );
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
        if (diff.vert < 0) return Movement.NORTH;
        return Movement.SOUTH;
    }

    private Movement getHorMovement(PosDiff diff) {
        if (diff.hor < 0) return Movement.WEST;
        return Movement.EAST;
    }


    void setState(Movement movement, boolean state) {
        PinState pinState = pinManager.getState(movement.pinRefCd);
        if (pinState.isHigh() != state) {
            if (state)
                LOGGER.log(Level.INFO, "Natáčam na " + movement.name + ".");
            else
                LOGGER.log(Level.INFO, "Zastavujem otáčanie na " + movement.name + ".");
            pinManager.setState(movement.pinRefCd, state);
        }
    }

    private void fireUpdate(AbsolutePosition currentPosition) {
        for (PositionChangeListener listener : listeners)
            listener.onUpdate(currentPosition);
    }

    public void addListener(PositionChangeListener listener) {
        listeners.add(listener);
    }


    private static class PosDiff {
        int hor;
        int vert;

        public PosDiff setHor(int hor) {
            this.hor = hor;
            return this;
        }

        public PosDiff setVert(int vert) {
            this.vert = vert;
            return this;
        }
    }


    private enum Movement {
        SOUTH(SOUTH_PIN_REF_CD, 1, "Juh"),
        NORTH(NORTH_PIN_REF_CD, -1, "Sever"),
        WEST(WEST_PIN_REF_CD, -1, "Západ"),
        EAST(EAST_PIN_REF_CD, 1, "Východ");

        private final String pinRefCd;
        private int tick;
        private final String name;
        private Movement shutdownFirst;

        static {
            SOUTH.shutdownFirst = NORTH;
            NORTH.shutdownFirst = SOUTH;
            WEST.shutdownFirst = EAST;
            EAST.shutdownFirst = WEST;
        }

        Movement(String pinRefCd, int tick, String name) {
            this.pinRefCd = pinRefCd;
            this.tick = tick;
            this.name = name;
        }
    }

    private static enum EventType {
        TICK,
        STOP;
    }

    private static class MoveEvent {
        private Movement movement;
        private EventType eventType;

        private MoveEvent(Movement movement, EventType eventType) {
            this.movement = movement;
            this.eventType = eventType;
        }
    }
}
