package org.dropco.smarthome.solar;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SolarPanelTest {

    @Test
    public void moveToSouth() throws InterruptedException {
        SolarPanelPosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanel panel = new SolarPanel(currentPosition, (cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        long beforeExecTime = System.currentTimeMillis();
        panel.move(position(-5, 0));
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
    public void moveWest() throws InterruptedException {
        SolarPanelPosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanel panel = new SolarPanel(currentPosition, (cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        long beforeExecTime = System.currentTimeMillis();
        panel.move(position(0, -5));
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
    public void moveSouthEast() throws InterruptedException {
        SolarPanelPosition currentPosition = position(0, -10);
        Set<String> result = new HashSet<>();
        SolarPanel panel = new SolarPanel(currentPosition, (cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        long beforeExecTime = System.currentTimeMillis();
        panel.move(position(-5, 0));
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