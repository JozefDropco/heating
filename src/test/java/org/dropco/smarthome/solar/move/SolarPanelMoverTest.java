package org.dropco.smarthome.solar.move;

import com.google.common.collect.Lists;
import org.dropco.smarthome.solar.SolarPanelPosition;
import org.dropco.smarthome.solar.SolarSystemRefCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
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
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(0, 5){
            @Override
            void sleep(int sleepTime) throws InterruptedException {
                sleepTimes.add(sleepTime);
            }
        }.run();
        Assert.assertTrue(sleepTimes.get(0) == 5);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }


    @Test
    public void moveWest() {
        SolarPanelPosition currentPosition = position(5, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(0,0){
            @Override
            void sleep(int sleepTime) throws InterruptedException {
                sleepTimes.add(sleepTime);
            }
        }.run();
        Assert.assertTrue(sleepTimes.get(0)== 5);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }
    @Test
    public void moveToNorth() {
        SolarPanelPosition currentPosition = position(0, 5);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });

        SolarPanelThreadManager.delaySupplier=()->1l;
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(0, 0){
            @Override
            void sleep(int sleepTime) throws InterruptedException {
                sleepTimes.add(sleepTime);
            }
        }.run();
        Assert.assertTrue(sleepTimes.get(0) == 5);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }


    @Test
    public void moveEast() {
        SolarPanelPosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(5,0){
            @Override
            void sleep(int sleepTime) throws InterruptedException {
                sleepTimes.add(sleepTime);
            }
        }.run();
        Assert.assertTrue(sleepTimes.get(0)== 5);
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void moveSouthEast() {
        SolarPanelPosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(280,135){
            @Override
            void sleep(int sleepTime) throws InterruptedException {
                sleepTimes.add(sleepTime);
            }
        }.run();
        Assert.assertTrue(sleepTimes.get(0)== 135);
        Assert.assertTrue(sleepTimes.get(1)== (280-135));
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
