package org.dropco.smarthome.heating.solar;

import org.dropco.smarthome.heating.solar.SolarSystemScheduledWork;
import org.dropco.smarthome.heating.solar.StrongWind;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.SolarPanelStepRecord;
import org.dropco.smarthome.heating.solar.move.SafetySolarPanel;
import org.dropco.smarthome.heating.solar.move.SolarPanelMover;
import org.dropco.smarthome.heating.solar.move.SolarPanelManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

public class SolarSystemScheduledWorkTest {

    private AbsolutePosition currentPos;

    @Test
    public void run() throws InterruptedException {

        SolarPanelManager.delaySupplier=()->1l;
        SolarPanelMover.setCurrentPositionSupplier(()-> (AbsolutePosition) startPos().getPosition());
        SolarPanelMover.setCommandExecutor((s, aBoolean) -> {});
        SolarPanelMover.addListener(position -> currentPos = position);
        SafetySolarPanel solarPanel =solarPanel( ()-> new DeltaPosition(-10,0));
        SolarSystemScheduledWork worker = new SolarSystemScheduledWork(solarPanel, true, new DeltaPosition(-10, 0));
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();
        Thread.sleep(11000);
        Assert.assertEquals(0,(long)currentPos.getVertical());
        Assert.assertEquals(-10,(long)currentPos.getHorizontal());

    }


    @Test
    public void runStrongWind() throws InterruptedException {

        SolarPanelManager.delaySupplier=()->1l;
        final AbsolutePosition[] currentPos = {new AbsolutePosition(-15, -30)};
        SolarPanelMover.setCommandExecutor((s, aBoolean) -> {});
        SolarPanelMover.setCurrentPositionSupplier(()-> currentPos[0]);
        SolarPanelMover.addListener(position -> {
            currentPos[0] =position;
        });
        SafetySolarPanel solarPanel =solarPanel(()->new DeltaPosition(0, -40));
        SolarSystemScheduledWork worker = new SolarSystemScheduledWork(solarPanel, true, new DeltaPosition(-20, 0));
        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(5 * 1000);
        StrongWind.set(true);
        solarPanel.moveToStrongWindPosition();
        worker = new SolarSystemScheduledWork(solarPanel, true, new DeltaPosition(-20, -200));
        thread = new Thread(worker);
        thread.start();
        thread.join();
        Thread.sleep(50000);
        Assert.assertEquals(0, currentPos[0].getHorizontal());
        Assert.assertEquals(-40, currentPos[0].getVertical());
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void runStrongWindBackToNormal() throws InterruptedException {

        SolarPanelManager.delaySupplier=()->1l;
        final AbsolutePosition[] currentPos = {new AbsolutePosition(-15, -30)};
        SolarPanelMover.setCommandExecutor((s, aBoolean) -> {});
        SolarPanelMover.setCurrentPositionSupplier(()-> currentPos[0]);
        SolarPanelMover.addListener(position -> {
            currentPos[0] =position;
        });
        SafetySolarPanel solarPanel =solarPanel(()->new DeltaPosition(0, -40));
        SolarSystemScheduledWork worker = new SolarSystemScheduledWork(solarPanel, true, new DeltaPosition(-20, 0));
        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(5 * 1000);
        StrongWind.set(true);
        solarPanel.moveToStrongWindPosition();
        worker = new SolarSystemScheduledWork(solarPanel, true, new DeltaPosition(0, 0));
        thread = new Thread(worker);
        thread.start();
        thread.join();
        Thread.sleep(50000);
        Assert.assertEquals(0,(long) currentPos[0].getHorizontal());
        Assert.assertEquals(-40,(long) currentPos[0].getVertical());
        Assert.assertFalse(thread.isAlive());
        solarPanel.backToNormal();
        Thread.sleep(45000);
        Assert.assertEquals(0, (long)currentPos[0].getHorizontal());
        Assert.assertEquals(0,(long) currentPos[0].getVertical());

    }

    private SafetySolarPanel solarPanel(Supplier<DeltaPosition> positionProvider) {
        return new SafetySolarPanel((p)->{}, null, null,null);
    }

    private SolarPanelStepRecord startPos() {
        return position(0, 0, 6, 10, 12, 1);
    }

    private SolarPanelStepRecord position(int horizontalPositionInSeconds, int verticalPositionInSeconds, int hour, int minute, int month, int day) {
        SolarPanelStepRecord currentPosition = new SolarPanelStepRecord();
        currentPosition.setPosition(new AbsolutePosition(horizontalPositionInSeconds,verticalPositionInSeconds));
        currentPosition.setHour(hour);
        currentPosition.setMinute(minute);
        return currentPosition;
    }
}
