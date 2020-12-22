package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.solar.SolarPanelPosition;
import org.dropco.smarthome.solar.SolarSystemRefCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SolarPanelMoverTest {

    @Test
    public void moveToSouth() {
        SolarPanelPosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelThreadManager.delaySupplier=()->1l;
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        long beforeExecTime = System.currentTimeMillis();
        new SolarPanelMover(-5, 0).run();
        long delta = System.currentTimeMillis() - beforeExecTime;
        long round = Math.round(delta / 1000.0);
        Assert.assertTrue(round >= 5);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }


    @Test
    public void moveWest() {
        SolarPanelPosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        long beforeExecTime = System.currentTimeMillis();
        new SolarPanelMover(0,-5).run();
        long delta = System.currentTimeMillis() - beforeExecTime;
        long round = Math.round(delta / 1000.0);
        Assert.assertTrue(round >= 5);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void moveSouthEast() {
        SolarPanelPosition currentPosition = position(0, -10);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        long beforeExecTime = System.currentTimeMillis();
        new SolarPanelMover(-5,0).run();
        long delta = System.currentTimeMillis() - beforeExecTime;
        long round = Math.round(delta / 1000.0);
        Assert.assertTrue(round >= 10);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + true));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }

    private SolarPanelPosition position(int horizontalPositionInSeconds, int verticalPositionInSeconds) {
        SolarPanelPosition panelPosition = new SolarPanelPosition();
        panelPosition.setHorizontalPositionInSeconds(horizontalPositionInSeconds);
        panelPosition.setVerticalPositionInSeconds(verticalPositionInSeconds);
        return panelPosition;
    }
}
