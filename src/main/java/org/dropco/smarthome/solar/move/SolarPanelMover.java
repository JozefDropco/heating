package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;
import org.dropco.smarthome.solar.SolarPanelPosition;

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
    private static Supplier<SolarPanelPosition> currentPositionSupplier;
    private static BiConsumer<String, Boolean> commandExecutor;
    private static List<PositionChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private Integer horizontal;
    private Integer vertical;

    SolarPanelMover(Integer horizontal, Integer vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public static void removeStopListener(CountDownWatcher countDownWatcher) {
    }

    @Override
    public void run() {
        SolarPanelPosition currentPosition = currentPositionSupplier.get();
        int diffHorizontal = 0;
        if (horizontal != null)
            diffHorizontal = horizontal - currentPosition.getHorizontalPositionInSeconds();
        int diffVertical = 0;
        if (vertical != null)
            diffVertical = vertical - currentPosition.getVerticalPositionInSeconds();

        int absHorizontal = abs(diffHorizontal);
        int absVertical = abs(diffVertical);
        boolean movingNorth = diffVertical < 0;
        setState(NORTH_PIN_REF_CD, movingNorth && diffVertical != 0, NORTH);
        setState(SOUTH_PIN_REF_CD, !movingNorth && diffVertical != 0, SOUTH);
        boolean movingWest = diffHorizontal < 0;
        setState(WEST_PIN_REF_CD, movingWest && diffHorizontal != 0, WEST);
        setState(EAST_PIN_REF_CD, !movingWest && diffHorizontal != 0, EAST);

        if (absVertical > 0) {
            addVertical(currentPosition, absVertical, movingNorth);
        }
        if (absHorizontal > 0) {
            addHorizontal(currentPosition, absHorizontal, movingWest);
        }
    }

    private void addVertical(SolarPanelPosition currentPosition, int absVertical, boolean movingNorth) {
        addWatch(absVertical, ticks -> {
            if (movingNorth) {
                currentPosition.setVerticalPositionInSeconds(currentPosition.getVerticalPositionInSeconds() - ticks);
                setState(NORTH_PIN_REF_CD, false, NORTH);
            } else {
                currentPosition.setVerticalPositionInSeconds(currentPosition.getVerticalPositionInSeconds() + ticks);
                setState(SOUTH_PIN_REF_CD, false, SOUTH);
            }
            fireUpdate(currentPosition);
        }, VerticalMoveFeedback.getInstance()::addRealTimeTicker, VerticalMoveFeedback.getInstance()::addSubscriber);
    }

    private void addHorizontal(SolarPanelPosition currentPosition, int absHorizontal, boolean movingWest) {
        addWatch(absHorizontal, ticks -> {
            if (movingWest) {
                currentPosition.setHorizontalPositionInSeconds(currentPosition.getHorizontalPositionInSeconds() - ticks);
                setState(WEST_PIN_REF_CD, false, WEST);
            } else {
                currentPosition.setHorizontalPositionInSeconds(currentPosition.getHorizontalPositionInSeconds() + ticks);
                setState(EAST_PIN_REF_CD, false, EAST);
            }
            fireUpdate(currentPosition);
        }, HorizontalMoveFeedback.getInstance()::addRealTimeTicker, HorizontalMoveFeedback.getInstance()::addSubscriber);
    }

    void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>,Boolean>> addMoveListener) {
        new CountDownWatcher(ticks).start(onceFinished, addRealTimeTicker, addMoveListener);
    }

    void setState(String pinRefCd, boolean state, String direction) {
        if (state)
            LOGGER.log(Level.FINE, "Natáčam na " + direction + ".");
        else
            LOGGER.log(Level.FINE, "Zastavujem otáčanie na " + direction + ".");
        commandExecutor.accept(pinRefCd, state);
    }

    private void fireUpdate(SolarPanelPosition currentPosition) {
        for (PositionChangeListener listener : listeners)
            listener.onUpdate(currentPosition);
    }

    public static void addListener(PositionChangeListener listener) {
        listeners.add(listener);
    }

    public static void setCurrentPositionSupplier(Supplier<SolarPanelPosition> currentPositionSupplier) {
        SolarPanelMover.currentPositionSupplier = currentPositionSupplier;
    }

    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        SolarPanelMover.commandExecutor = commandExecutor;
    }

}
