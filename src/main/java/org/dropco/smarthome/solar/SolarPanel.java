package org.dropco.smarthome.solar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.lang.Math.*;

public class SolarPanel {
    private SolarPanelPosition currentPosition;
    private BiConsumer<String, Boolean> commandExecutor;
    private Supplier<Boolean> shouldTerminate;
    private List<PositionChangeListener> listeners = new ArrayList<>();

    public SolarPanel(SolarPanelPosition currentPosition, BiConsumer<String, Boolean> commandExecutor) {
        this.currentPosition = currentPosition;
        this.commandExecutor = commandExecutor;
    }


    public void move(SolarPanelPosition newPosition) throws InterruptedException {
        int diffHorizontal = 0;
        if (newPosition.getHorizontalPositionInSeconds() != null)
            diffHorizontal = newPosition.getHorizontalPositionInSeconds() - currentPosition.getHorizontalPositionInSeconds();
        int diffVertical = 0;
        if (newPosition.getVerticalPositionInSeconds() != null)
            diffVertical = newPosition.getVerticalPositionInSeconds() - currentPosition.getVerticalPositionInSeconds();
        move(diffHorizontal, diffVertical, false);
    }

    private void move(int horizontal, int vertical, boolean rest) throws InterruptedException {
        if (!rest) {
            commandExecutor.accept(SolarSystemRefCode.SOUTH_PIN_REF_CD, false);
            commandExecutor.accept(SolarSystemRefCode.NORTH_PIN_REF_CD, false);
            commandExecutor.accept(SolarSystemRefCode.WEST_PIN_REF_CD, false);
            commandExecutor.accept(SolarSystemRefCode.EAST_PIN_REF_CD, false);
        }
        //stop any movement
        int absHorizontal = abs(horizontal);
        int absVertical = abs(vertical);
        int sleepTime = min(absHorizontal, absVertical);
        if (sleepTime == 0) {
            sleepTime = max(absHorizontal, absVertical);
        }

        if (horizontal < 0) commandExecutor.accept(SolarSystemRefCode.SOUTH_PIN_REF_CD, true);
        if (horizontal > 0) commandExecutor.accept(SolarSystemRefCode.NORTH_PIN_REF_CD, true);
        if (vertical < 0) commandExecutor.accept(SolarSystemRefCode.WEST_PIN_REF_CD, true);
        if (vertical > 0) commandExecutor.accept(SolarSystemRefCode.EAST_PIN_REF_CD, true);

        while (sleepTime > 0) {
            Thread.sleep(1000);
            if (shouldTerminate.get()) return;
            sleepTime--;
            if (horizontal < 0) {
                currentPosition.setHorizontalPositionInSeconds(currentPosition.getHorizontalPositionInSeconds() - 1);
                fireUpdate();
                horizontal++;
            }
            if (horizontal > 0) {
                currentPosition.setHorizontalPositionInSeconds(currentPosition.getHorizontalPositionInSeconds() + 1);
                fireUpdate();
                horizontal--;
            }
            if (vertical < 0) {
                currentPosition.setVerticalPositionInSeconds(currentPosition.getVerticalPositionInSeconds() - 1);
                fireUpdate();
                vertical++;
            }
            if (vertical > 0) {
                currentPosition.setVerticalPositionInSeconds(currentPosition.getVerticalPositionInSeconds() + 1);
                fireUpdate();
                vertical--;
            }
        }
        if (horizontal != 0 || vertical != 0) move(horizontal, vertical, true);
        commandExecutor.accept(SolarSystemRefCode.SOUTH_PIN_REF_CD, false);
        commandExecutor.accept(SolarSystemRefCode.NORTH_PIN_REF_CD, false);
        commandExecutor.accept(SolarSystemRefCode.WEST_PIN_REF_CD, false);
        commandExecutor.accept(SolarSystemRefCode.EAST_PIN_REF_CD, false);
    }

    private void fireUpdate() {
        for (PositionChangeListener listener : listeners)
            listener.onUpdate(this);
    }

    public SolarPanelPosition getCurrentPosition() {
        return currentPosition;
    }

    public void setShouldTerminate(Supplier<Boolean> shouldTerminate) {
        this.shouldTerminate = shouldTerminate;
    }

    public void addListener(PositionChangeListener listener) {
        listeners.add(listener);
    }
}
