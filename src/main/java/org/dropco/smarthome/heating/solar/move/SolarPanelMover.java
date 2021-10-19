package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.ImmutableMap;
import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;
import org.dropco.smarthome.heating.dto.AbsolutePosition;
import org.dropco.smarthome.heating.dto.DeltaPosition;
import org.dropco.smarthome.heating.dto.Position;
import org.dropco.smarthome.heating.dto.PositionProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static org.dropco.smarthome.heating.solar.SolarSystemRefCode.*;

public class SolarPanelMover implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SolarPanelMover.class.getName());
    private static final Map<String, String> translationMap = ImmutableMap.of(WEST_PIN_REF_CD,"Západ",
                                                                              EAST_PIN_REF_CD, "Východ",
                                                                              NORTH_PIN_REF_CD, "Sever",
                                                                              SOUTH_PIN_REF_CD, "Juh");
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
        LOGGER.fine("Posun o [hor="+diffHorizontal+", vert="+diffVertical+"]");
        if (absHorizontal > 0) {
            boolean movingWest = diffHorizontal < 0;
            setState(WEST_PIN_REF_CD, movingWest);
            setState(EAST_PIN_REF_CD, !movingWest);
            LOGGER.fine("Pridavam kontrolu horizontalneho posunu");
            addHorizontal(currentPositionSupplier.get(), absHorizontal, movingWest);
        }
        int absVertical = abs(diffVertical);
        if (absVertical > 0) {
            boolean movingNorth = diffVertical < 0;
            setState(NORTH_PIN_REF_CD, movingNorth);
            setState(SOUTH_PIN_REF_CD, !movingNorth);
            LOGGER.fine("Pridavam kontrolu vertikalneho posunu");
            addVertical(currentPositionSupplier.get(), absVertical, movingNorth);
        }
    }

    private void addVertical(AbsolutePosition currentPosition, int absVertical, boolean movingNorth) {
        addWatch(absVertical, ticks -> {
            if (movingNorth) {
                currentPosition.setVertical(currentPosition.getVertical() - ticks);
                setState(NORTH_PIN_REF_CD, false);
            } else {
                currentPosition.setVertical(currentPosition.getVertical() + ticks);
                setState(SOUTH_PIN_REF_CD, false);
            }
            fireUpdate(currentPosition);
        }, VerticalMoveFeedback.getInstance()::addRealTimeTicker, VerticalMoveFeedback.getInstance()::addSubscriber,
                VerticalMoveFeedback::getMoving);
    }

    private void addHorizontal(AbsolutePosition currentPosition, int absHorizontal, boolean movingWest) {
        addWatch(absHorizontal, ticks -> {
            if (movingWest) {
                currentPosition.setHorizontal(currentPosition.getHorizontal() - ticks);
                setState(WEST_PIN_REF_CD, false);
            } else {
                currentPosition.setHorizontal(currentPosition.getHorizontal() + ticks);
                setState(EAST_PIN_REF_CD, false);
            }
            fireUpdate(currentPosition);
        }, HorizontalMoveFeedback.getInstance()::addRealTimeTicker, HorizontalMoveFeedback.getInstance()::addSubscriber,
                HorizontalMoveFeedback::getMoving);
    }

   void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>,Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
        new CountDownWatcher(ticks).start(onceFinished, addRealTimeTicker, addMoveListener,isMoving);
    }

    void setState(String pinRefCd, boolean state) {
        if (state)
            LOGGER.log(Level.INFO, "Natáčam na " + translationMap.get(pinRefCd) + ".");
        else
            LOGGER.log(Level.INFO, "Zastavujem otáčanie na " + translationMap.get(pinRefCd) + ".");
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
