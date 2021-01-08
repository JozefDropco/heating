package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.solar.SolarPanelPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.*;
import static org.dropco.smarthome.solar.SolarSystemRefCode.*;

public class SolarPanelMover implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SolarPanelMover.class.getName());
    protected static final String WEST = "Západ";
    protected static final String EAST = "Východ";
    protected static final String SOUTH = "Juh";
    protected static final String NORTH = "Sever";
    private static Supplier<SolarPanelPosition> currentPositionSupplier;
    private static BiConsumer<String, Boolean> commandExecutor;
    private static List<PositionChangeListener> listeners = new ArrayList<>();
    private Integer horizontal;
    private Integer vertical;

    SolarPanelMover(Integer horizontal, Integer vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
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

        setState(NORTH_PIN_REF_CD, diffVertical < 0, NORTH, diffVertical < 0);
        setState(SOUTH_PIN_REF_CD, diffVertical > 0, SOUTH, diffVertical > 0);
        setState(WEST_PIN_REF_CD, diffHorizontal < 0, WEST, diffHorizontal < 0);
        setState(EAST_PIN_REF_CD, diffHorizontal > 0, EAST, diffHorizontal > 0);
        int absHorizontal = abs(diffHorizontal);
        int absVertical = abs(diffVertical);
        //Take minimum of shifts
        int sleepTime = min(absHorizontal, absVertical);
        //if we are not moving in 2 axis take the maximum
        if (sleepTime == 0) {
            sleepTime = max(absHorizontal, absVertical);
        }
        if (sleepTime > 0) {
            boolean interrupted = false;
            while (sleepTime > 0) {
                int secondsSlept = sleepTime;
                long milis = System.currentTimeMillis();
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    interrupted = true;
                    secondsSlept = Math.round((System.currentTimeMillis() - milis) / 1000);
                } finally {
                    if (diffHorizontal < 0) {
                        currentPosition.setHorizontalPositionInSeconds(currentPosition.getHorizontalPositionInSeconds() - secondsSlept);
                        diffHorizontal += secondsSlept;
                        if (diffHorizontal >= 0) {
                            diffHorizontal = 0;
                            setState(WEST_PIN_REF_CD, false, WEST, true);
                        }
                    } else {
                        if (diffHorizontal > 0) {
                            currentPosition.setHorizontalPositionInSeconds(currentPosition.getHorizontalPositionInSeconds() + secondsSlept);
                            diffHorizontal -= secondsSlept;
                            if (diffHorizontal <= 0) {
                                diffHorizontal = 0;
                                setState(EAST_PIN_REF_CD, false, EAST, true);
                            }
                        }
                    }
                    if (diffVertical > 0) {
                        currentPosition.setVerticalPositionInSeconds(currentPosition.getVerticalPositionInSeconds() + secondsSlept);
                        diffVertical -= secondsSlept;
                        if (diffVertical <= 0) {
                            diffVertical = 0;
                            setState(SOUTH_PIN_REF_CD, false, SOUTH, true);
                        }
                    } else {
                        if (diffVertical < 0) {
                            currentPosition.setVerticalPositionInSeconds(currentPosition.getVerticalPositionInSeconds() - secondsSlept);
                            diffVertical += secondsSlept;
                            if (diffVertical >= 0) {
                                diffVertical = 0;
                                setState(NORTH_PIN_REF_CD, false, NORTH, true);
                            }
                        }
                    }
                }
                if (!Thread.interrupted() && !interrupted) {
                    sleepTime = Math.max(abs(diffHorizontal), abs(diffVertical));
                } else {
                    setState(SOUTH_PIN_REF_CD, false, SOUTH, true);
                    setState(NORTH_PIN_REF_CD, false, NORTH, true);
                    setState(WEST_PIN_REF_CD, false, WEST, true);
                    setState(EAST_PIN_REF_CD, false, EAST, true);
                    break;
                }
            }
            fireUpdate(currentPosition);
        }
    }

    void sleep(int sleepTime) throws InterruptedException {
        Thread.sleep(sleepTime * 1000);
    }

    void setState(String pinRefCd, boolean state, String direction, boolean changeState) {
        if (changeState) {
            if (state)
                LOGGER.log(Level.INFO, "Natáčam na " + direction + ".");
            else
                LOGGER.log(Level.INFO, "Zastavujem otáčanie na " + direction + ".");
        }
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
