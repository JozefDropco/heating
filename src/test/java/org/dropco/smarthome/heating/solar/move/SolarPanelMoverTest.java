package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SolarPanelMoverTest {

    @Test
    public void moveToSouth() {
        AbsolutePosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });

        SolarPanelManager.delaySupplier=()->1l;
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(new AbsolutePosition(0,5)){

            @Override
            void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>, Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
                sleepTimes.add(ticks);
                onceFinished.accept(ticks);
            }
        }.run();
        Assert.assertTrue(sleepTimes.contains(5));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }


    @Test
    public void moveWest() {
        AbsolutePosition currentPosition = position(5, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(new AbsolutePosition(0,0)){
            @Override
            void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>, Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
                sleepTimes.add(ticks);
                onceFinished.accept(ticks);
            }
        }.run();
        Assert.assertTrue(sleepTimes.contains(5));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }
    @Test
    public void moveToNorth() {
        AbsolutePosition currentPosition = position(0, 5);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });

        SolarPanelManager.delaySupplier=()->1l;
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(new AbsolutePosition(0, 0)){
            @Override
            void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>, Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
                sleepTimes.add(ticks);
                onceFinished.accept(ticks);
            }
        }.run();
        Assert.assertTrue(sleepTimes.contains(5));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }


    @Test
    public void moveEast() {
        AbsolutePosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(new AbsolutePosition(5,0)){
            @Override
            void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>, Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
                sleepTimes.add(ticks);
                onceFinished.accept(ticks);
            }
        }.run();
        Assert.assertTrue(sleepTimes.contains(5));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void moveSouthEast() {
        AbsolutePosition currentPosition = position(0, 0);
        Set<String> result = new HashSet<>();
        SolarPanelMover.setCommandExecutor((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        });
        SolarPanelMover.setCurrentPositionSupplier(()->currentPosition);
        List<Integer> sleepTimes = Lists.newArrayList();
        new SolarPanelMover(new AbsolutePosition(280,135)){
            @Override
            void addWatch(int ticks, Consumer<Integer> onceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> addRealTimeTicker, Consumer<BiConsumer<Supplier<Boolean>, Boolean>> addMoveListener, Supplier<Boolean> isMoving) {
                sleepTimes.add(ticks);
                onceFinished.accept(ticks);
            }
        }.run();
        Assert.assertTrue(sleepTimes.contains(135));
        Assert.assertTrue(sleepTimes.contains(280));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + true));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }

    private AbsolutePosition position(int horizontalPositionInSeconds, int verticalPositionInSeconds) {
        AbsolutePosition panelPosition = new AbsolutePosition(horizontalPositionInSeconds,verticalPositionInSeconds);
        return panelPosition;
    }
}
