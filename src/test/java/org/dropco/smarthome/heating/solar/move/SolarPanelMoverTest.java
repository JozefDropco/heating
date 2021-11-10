package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import org.dropco.smarthome.MainTest;
import org.dropco.smarthome.TickerPin;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
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
        TickerPin verticalTickPin = new TickerPin(500, 5);
        TickerPin horizontalTickPin = new TickerPin(500, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        MockedPinManager mockedPinManager = new MockedPinManager();
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        verticalTickPin.startTicking();
        mover.moveTo("1", position(0, 5));
        pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to south", 5, updatedPosition.get().getVertical());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.SOUTH_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.SOUTH_PIN_REF_CD,true));
    }

    @Test
    public void moveToNorth() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(500, 5);
        TickerPin horizontalTickPin = new TickerPin(500, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 5);
        MockedPinManager mockedPinManager = new MockedPinManager();
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        verticalTickPin.startTicking();
        mover.moveTo("1", position(0, 0));
        pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to north", 0, updatedPosition.get().getVertical());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.NORTH_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.NORTH_PIN_REF_CD,true));
    }



    @Test
    public void moveToWest() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(500, 5);
        TickerPin horizontalTickPin = new TickerPin(500, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(5, 0);
        MockedPinManager mockedPinManager = new MockedPinManager();
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        horizontalTickPin.startTicking();
        mover.moveTo("1", position(0, 0));
        pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to west", 0, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.WEST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.WEST_PIN_REF_CD,true));
    }


    @Test
    public void moveToEast() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(500, 5);
        TickerPin horizontalTickPin = new TickerPin(500, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        MockedPinManager mockedPinManager = new MockedPinManager();
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        horizontalTickPin.startTicking();
        mover.moveTo("1", position(5, 0));
        pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD,true));
    }

    @Test
    public void moveToTwice() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(500, 5);
        TickerPin horizontalTickPin = new TickerPin(500, 10);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AtomicReference<AbsolutePosition> currentPosition = new AtomicReference<>();
        currentPosition.set(position(0, 0));
        MockedPinManager mockedPinManager = new MockedPinManager(){
            @Override
            public void setState(String key, boolean value) {
                if(value){
                    switch (key){
                        case SolarSystemRefCode.EAST_PIN_REF_CD:
                        case SolarSystemRefCode.WEST_PIN_REF_CD:
                            horizontalTickPin.startTicking();
                            break;

                        case SolarSystemRefCode.NORTH_PIN_REF_CD:
                        case SolarSystemRefCode.SOUTH_PIN_REF_CD:
                            verticalTickPin.startTicking();
                            break;
                        default:
                    }
                } else {
                    switch (key){
                        case SolarSystemRefCode.EAST_PIN_REF_CD:
                        case SolarSystemRefCode.WEST_PIN_REF_CD:
                            horizontalTickPin.stopTicking();
                            break;

                        case SolarSystemRefCode.NORTH_PIN_REF_CD:
                        case SolarSystemRefCode.SOUTH_PIN_REF_CD:
                            verticalTickPin.stopTicking();
                            break;
                        default:
                    }
                }
                super.setState(key, value);
            }
        };
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition.get(), verticalMoveFeedback, horizontalMoveFeedback);
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                currentPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        mover.moveTo("1", position(5, 0));
        mover.moveTo("2",position(5,5));
        pool.awaitTermination(10, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 5, currentPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, currentPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD,true));
    }

    @Test
    public void lessTicksThanExpected() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(500, 5);
        TickerPin horizontalTickPin = new TickerPin(500, 3);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        MockedPinManager mockedPinManager = new MockedPinManager();
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(mover);
        horizontalTickPin.startTicking();
        mover.moveTo("1", position(5, 0));
        pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 3 ticks to east", 3, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD,true));
    }
    private AbsolutePosition position(int horizontalPositionInSeconds, int verticalPositionInSeconds) {
        AbsolutePosition panelPosition = new AbsolutePosition(horizontalPositionInSeconds, verticalPositionInSeconds);
        return panelPosition;
    }

}
