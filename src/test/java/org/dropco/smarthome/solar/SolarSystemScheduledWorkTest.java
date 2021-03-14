package org.dropco.smarthome.solar;

import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelMover;
import org.dropco.smarthome.solar.move.SolarPanelManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

public class SolarSystemScheduledWorkTest {

    private SolarPanelPosition currentPos;

    @Test
    public void run() throws InterruptedException {

        SolarPanelManager.delaySupplier=()->1l;
        SolarPanelMover.setCurrentPositionSupplier(startPos()::getPanelPosition);
        SolarPanelMover.setCommandExecutor((s, aBoolean) -> {});
        SolarPanelMover.addListener(position -> currentPos = position);
        SafetySolarPanel solarPanel =solarPanel( startPos()::getPanelPosition);
        SolarSystemScheduledWork worker = new SolarSystemScheduledWork(solarPanel, true, new SolarPanelPosition(0,-10));
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();
        Thread.sleep(11000);
        Assert.assertEquals(0,(long)currentPos.getVerticalPositionInSeconds());
        Assert.assertEquals(-10,(long)currentPos.getHorizontalPositionInSeconds());

    }

    @Test
    public void runStrongWind() throws InterruptedException {

        SolarPanelManager.delaySupplier=()->1l;
        final SolarPanelPosition[] currentPos = {new SolarPanelPosition(-30, -15)};
        SolarPanelMover.setCommandExecutor((s, aBoolean) -> {});
        SolarPanelMover.setCurrentPositionSupplier(()-> currentPos[0]);
        SolarPanelMover.addListener(position -> {
            currentPos[0] =position;
        });
        SafetySolarPanel solarPanel =solarPanel(()->new SolarPanelPosition(-40,0));
        SolarSystemScheduledWork worker = new SolarSystemScheduledWork(solarPanel, true, new SolarPanelPosition(0,-20));
        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(5 * 1000);
        StrongWind.set(true);
        solarPanel.moveToStrongWindPosition();
        worker = new SolarSystemScheduledWork(solarPanel, true, new SolarPanelPosition(-200,-20));
        thread = new Thread(worker);
        thread.start();
        thread.join();
        Thread.sleep(50000);
        Assert.assertEquals(0, (long)currentPos[0].getHorizontalPositionInSeconds());
        Assert.assertEquals(-40,(long) currentPos[0].getVerticalPositionInSeconds());
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void runStrongWindBackToNormal() throws InterruptedException {

        SolarPanelManager.delaySupplier=()->1l;
        final SolarPanelPosition[] currentPos = {new SolarPanelPosition(-30, -15)};
        SolarPanelMover.setCommandExecutor((s, aBoolean) -> {});
        SolarPanelMover.setCurrentPositionSupplier(()-> currentPos[0]);
        SolarPanelMover.addListener(position -> {
            currentPos[0] =position;
        });
        SafetySolarPanel solarPanel =solarPanel(()->new SolarPanelPosition(-40,0));
        SolarSystemScheduledWork worker = new SolarSystemScheduledWork(solarPanel, true, new SolarPanelPosition(0,-20));
        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(5 * 1000);
        StrongWind.set(true);
        solarPanel.moveToStrongWindPosition();
        worker = new SolarSystemScheduledWork(solarPanel, true, new SolarPanelPosition(0,0));
        thread = new Thread(worker);
        thread.start();
        thread.join();
        Thread.sleep(50000);
        Assert.assertEquals(0,(long) currentPos[0].getHorizontalPositionInSeconds());
        Assert.assertEquals(-40,(long) currentPos[0].getVerticalPositionInSeconds());
        Assert.assertFalse(thread.isAlive());
        solarPanel.backToNormal();
        Thread.sleep(45000);
        Assert.assertEquals(0, (long)currentPos[0].getHorizontalPositionInSeconds());
        Assert.assertEquals(0,(long) currentPos[0].getVerticalPositionInSeconds());

    }

    private SafetySolarPanel solarPanel(Supplier<SolarPanelPosition> positionProvider) {
        return new SafetySolarPanel(positionProvider);
    }

    private SolarPanelStepRecord startPos() {
        return position(0, 0, 6, 10, 12, 1);
    }

    private SolarPanelStepRecord position(int horizontalPositionInSeconds, int verticalPositionInSeconds, int hour, int minute, int month, int day) {
        SolarPanelStepRecord currentPosition = new SolarPanelStepRecord();
        SolarPanelPosition position = new SolarPanelPosition();
        position.setHorizontalPositionInSeconds(horizontalPositionInSeconds);
        position.setVerticalPositionInSeconds(verticalPositionInSeconds);
        currentPosition.setPanelPosition(position);
        currentPosition.setHour(hour);
        currentPosition.setMinute(minute);
        currentPosition.setMonth(month);
        currentPosition.setDay(day);
        return currentPosition;
    }
}
