package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import org.dropco.smarthome.MainTest;
import org.dropco.smarthome.TickerPin;
import org.dropco.smarthome.heating.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SolarPanelMoverTest {


    @BeforeClass
    public static void initOnce() throws NoSuchFieldException, IllegalAccessException {
        MainTest.simulate();
    }

    @Before
    public void beforeTest() {
        }

    @Test
    public void moveToSouth() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        List<String> result = Lists.newArrayList();
        SolarPanelMover mover = new SolarPanelMover((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        }, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback, () -> 1l);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        mover.moveTo(position(0, 5));
        verticalTickPin.startTicking();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to south", 5, updatedPosition.get().getVertical());
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void moveToNorth() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 5);
        List<String> result = Lists.newArrayList();
        SolarPanelMover mover = new SolarPanelMover((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        }, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback, () -> 1l);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        mover.moveTo(position(0, 0));
        verticalTickPin.startTicking();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to north", 0, updatedPosition.get().getVertical());
        Assert.assertTrue(result.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }



    @Test
    public void moveToWest() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(5, 0);
        List<String> result = Lists.newArrayList();
        SolarPanelMover mover = new SolarPanelMover((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        }, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback, () -> 1l);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        mover.moveTo(position(0, 0));
        horizontalTickPin.startTicking();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to west", 0, updatedPosition.get().getHorizontal());
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }


    @Test
    public void moveToEast() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        List<String> result = Lists.newArrayList();
        SolarPanelMover mover = new SolarPanelMover((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        }, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback, () -> 1l);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        mover.moveTo(position(5, 0));
        horizontalTickPin.startTicking();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, updatedPosition.get().getHorizontal());
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }



    @Test
    public void lessTicksThanExpected() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 3);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        List<String> result = Lists.newArrayList();
        SolarPanelMover mover = new SolarPanelMover((cmdRefCd, value) -> {
            result.add(cmdRefCd + value);
        }, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback, () -> 1l);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        mover.moveTo(position(5, 0));
        horizontalTickPin.startTicking();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 3, updatedPosition.get().getHorizontal());
        Assert.assertTrue(result.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(result.remove(SolarSystemRefCode.EAST_PIN_REF_CD + true));
        Assert.assertTrue(result.isEmpty());
    }
    private AbsolutePosition position(int horizontalPositionInSeconds, int verticalPositionInSeconds) {
        AbsolutePosition panelPosition = new AbsolutePosition(horizontalPositionInSeconds, verticalPositionInSeconds);
        return panelPosition;
    }
}
