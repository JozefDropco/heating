package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;
import org.dropco.smarthome.solar.dto.AbsolutePosition;
import org.dropco.smarthome.solar.dto.DeltaPosition;
import org.dropco.smarthome.solar.dto.Position;
import org.dropco.smarthome.solar.dto.PositionProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static org.dropco.smarthome.solar.SolarSystemRefCode.*;

public class SolarPanelMover implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SolarPanelMover.class.getName());
    protected static final String WEST = "Západ";
    protected static final String EAST = "Východ";
    protected static final String SOUTH = "Juh";
    protected static final String NORTH = "Sever";
    private static Supplier<AbsolutePosition> currentPositionSupplier;
    private static BiConsumer<String, Boolean> commandExecutor;
    private static List<PositionChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private Position position;

    private int diffHorizontal = 0;
    private int diffVertical = 0;

    SolarPanelMover(Position position) {
        this.position = position;
    }

    @Override
    public void run() {
        position.invoke(new PositionProcessor() {
            @Override
            public void process(AbsolutePosition absPos) {
                AbsolutePosition currentPosition = currentPositionSupplier.get();
                diffHorizontal = absPos.getHorizontal() - currentPosition.getHorizontal();
                diffVertical = absPos.getVertical() - currentPosition.getVertical();
            }

            @Override
            public void process(DeltaPosition deltaPos) {
                diffHorizontal = deltaPos.getDeltaHorizontalTicks();
                diffVertical = deltaPos.getDeltaVerticalTicks();
            }
        });
        int absHorizontal = abs(diffHorizontal);
        int absVertical = abs(diffVertical);
        boolean movingNorth = diffVertical < 0;
        setState(NORTH_PIN_REF_CD, movingNorth && diffVertical != 0, NORTH);
        setState(SOUTH_PIN_REF_CD, !movingNorth && diffVertical != 0, SOUTH);
        boolean movingWest = diffHorizontal < 0;
        setState(WEST_PIN_REF_CD, movingWest && diffHorizontal != 0, WEST);
        setState(EAST_PIN_REF_CD, !movingWest && diffHorizontal != 0, EAST);

        if (absVertical > 0) {
            addVertical(currentPositionSupplier.get(), absVertical, movingNorth);
        }
        if (absHorizontal > 0) {
            addHorizontal(currentPositionSupplier.get(), absHorizontal, movingWest);
        }
    }

    private void addVertical(AbsolutePosition currentPosition, int absVertical, boolean movingNorth) {
        addWatch(absVertical, ticks -> {
            if (movingNorth) {
                currentPosition.setVertical(currentPosition.getVertical() - ticks);
                setState(NORTH_PIN_REF_CD, false, NORTH);
            } else {
                currentPosition.setVertical(currentPosition.getVertical() + ticks);
                setState(SOUTH_PIN_REF_CD, false, SOUTH);
            }
            fireUpdate(currentPosition);
        }, VerticalMoveFeedback.getInstance()::addRealTimeTicker, VerticalMoveFeedback.getInstance()::addSubscriber,
                VerticalMoveFeedback::getMoving);
    }

    private void addHorizontal(AbsolutePosition currentPosition, int absHorizontal, boolean movingWest) {
        addWatch(absHorizontal, ticks -> {
            if (movingWest) {
                currentPosition.setHorizontal(currentPosition.getHorizontal() - ticks);
                setState(WEST_PIN_REF_CD, false, WEST);
            } else {
                currentPosition.setHorizontal(currentPosition.getHorizontal() + ticks);
                setState(EAST_PIN_REF_CD, false, EAST);
            }
            fireUpdate(currentPosition);
        }, HorizontalMoveFeedback.getInstance()::addRealTimeTicker, HorizontalMoveFeedback.getInstance()::addSubscriber,
                HorizontalMoveFeedback::getMoving);
    }

   void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>,Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
        new CountDownWatcher(ticks).start(onceFinished, addRealTimeTicker, addMoveListener,isMoving);
    }

    void setState(String pinRefCd, boolean state, String direction) {
        if (state)
            LOGGER.log(Level.INFO, "Natáčam na " + direction + ".");
        else
            LOGGER.log(Level.INFO, "Zastavujem otáčanie na " + direction + ".");
        commandExecutor.accept(pinRefCd, state);
    }

    private void fireUpdate(AbsolutePosition currentPosition) {
        for (PositionChangeListener listener : listeners)
            listener.onUpdate(currentPosition);
    }

    public static void addListener(PositionChangeListener listener) {
        listeners.add(listener);
    }

    public static void setCurrentPositionSupplier(Supplier<AbsolutePosition> currentPositionSupplier) {
        SolarPanelMover.currentPositionSupplier = currentPositionSupplier;
    }

    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        SolarPanelMover.commandExecutor = commandExecutor;
    }

}
