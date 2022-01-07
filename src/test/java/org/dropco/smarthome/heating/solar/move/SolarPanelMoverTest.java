package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.MainTest;
import org.dropco.smarthome.TickerPin;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        TickerPin verticalTickPin = new TickerPin(200, 5);
        TickerPin horizontalTickPin = new TickerPin(200, 0);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AbsolutePosition currentPosition = position(0, 0);
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, v->{}, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                Logger.getLogger(SolarPanelMoverTest.class.getName()).log(Level.INFO, "Update " + position);
                updatedPosition.set(position);
            }
        });
        mover.connect();
        mover.moveTo("1", position(0, 5));
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to south", 5, updatedPosition.get().getVertical());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.SOUTH_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.SOUTH_PIN_REF_CD, true));
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
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, v->{}, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        mover.connect();
        mover.moveTo("1", position(0, 0));
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to north", 0, updatedPosition.get().getVertical());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.NORTH_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.NORTH_PIN_REF_CD, true));
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
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, v->{}, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        mover.connect();
        mover.moveTo("1", position(0, 0));
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to west", 0, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.WEST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.WEST_PIN_REF_CD, true));
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
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, v->{}, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        mover.connect();
        mover.moveTo("1", position(5, 0));
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD, true));
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
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition.get(), v->{}, verticalMoveFeedback, horizontalMoveFeedback);
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                currentPosition.set(position);
            }
        });
        mover.connect();
        mover.moveTo("1", position(5, 0));
        mover.waitForEnd();
        mover.moveTo("2", position(5, 5));
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 5, currentPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, currentPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD, true));
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
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        SolarPanelMover mover = new SolarPanelMover(mockedPinManager, () -> currentPosition, v->{}, verticalMoveFeedback, horizontalMoveFeedback);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>();
        mover.addListener(new PositionChangeListener() {
            @Override
            public void onUpdate(AbsolutePosition position) {
                updatedPosition.set(position);
            }
        });
        mover.connect();
        mover.moveTo("1", position(5, 0));
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 3 ticks to east", 3, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD, true));
    }

    private AbsolutePosition position(int horizontalPositionInSeconds, int verticalPositionInSeconds) {
        AbsolutePosition panelPosition = new AbsolutePosition(horizontalPositionInSeconds, verticalPositionInSeconds);
        return panelPosition;
    }

}
