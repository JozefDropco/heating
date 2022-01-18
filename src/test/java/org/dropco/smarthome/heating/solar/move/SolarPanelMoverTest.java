package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.MainTest;
import org.dropco.smarthome.TickerPin;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.move.horizontal.HorizontalMove;
import org.dropco.smarthome.heating.solar.move.horizontal.HorizontalMoveFeedback;
import org.dropco.smarthome.heating.solar.move.vertical.VerticalMove;
import org.dropco.smarthome.heating.solar.move.vertical.VerticalMoveFeedback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
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
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 0);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);
        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>(position(0, 0));

        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        HorizontalMove horizontalMove = new HorizontalMove(horizontalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int hor = updatedPosition.get().getHorizontal() + tick;
            updatedPosition.get().setHorizontal(hor);
        }, (v) -> {
        });
        VerticalMove verticalMove = new VerticalMove(verticalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int vert = updatedPosition.get().getVertical() + tick;
            updatedPosition.get().setVertical(vert);
        }, (v) -> {
        });
        horizontalMove.start();
        verticalMove.start();
        SolarPanelMover mover = new SolarPanelMover(() -> updatedPosition.get(), horizontalMove, verticalMove);

        mover.moveTo("1", position(0, 5));
        Thread.sleep(200);
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to south", 5, updatedPosition.get().getVertical());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.SOUTH_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.SOUTH_PIN_REF_CD, true));
    }

    @Test
    public void moveToNorth() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>(position(0, 5));
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        HorizontalMove horizontalMove = new HorizontalMove(horizontalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int hor = updatedPosition.get().getHorizontal() + tick;
            updatedPosition.get().setHorizontal(hor);
        }, (v) -> {
        });
        VerticalMove verticalMove = new VerticalMove(verticalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int vert = updatedPosition.get().getVertical() + tick;
            updatedPosition.get().setVertical(vert);
        }, (v) -> {
        });
        horizontalMove.start();
        verticalMove.start();
        SolarPanelMover mover = new SolarPanelMover(() -> updatedPosition.get(), horizontalMove, verticalMove);
        mover.moveTo("1", position(0, 0));
        Thread.sleep(200);
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getHorizontal());
        Assert.assertEquals("We should move 5 ticks to north", 0, updatedPosition.get().getVertical());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.NORTH_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.NORTH_PIN_REF_CD, true));
    }


    @Test
    public void moveToWest() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>(position(5, 0));
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        HorizontalMove horizontalMove = new HorizontalMove(horizontalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int hor = updatedPosition.get().getHorizontal() + tick;
            updatedPosition.get().setHorizontal(hor);
        }, (v) -> {
        });
        VerticalMove verticalMove = new VerticalMove(verticalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int vert = updatedPosition.get().getVertical() + tick;
            updatedPosition.get().setVertical(vert);
        }, (v) -> {
        });
        horizontalMove.start();
        verticalMove.start();
        SolarPanelMover mover = new SolarPanelMover(() -> updatedPosition.get(), horizontalMove, verticalMove);
        mover.moveTo("1", position(0, 0));
        Thread.sleep(200);
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to west", 0, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.WEST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.WEST_PIN_REF_CD, true));
    }


    @Test
    public void moveToEast() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 5);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>(position(0, 0));
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        HorizontalMove horizontalMove = new HorizontalMove(horizontalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int hor = updatedPosition.get().getHorizontal() + tick;
            updatedPosition.get().setHorizontal(hor);
        }, (v) -> {
        });
        VerticalMove verticalMove = new VerticalMove(verticalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int vert = updatedPosition.get().getVertical() + tick;
            updatedPosition.get().setVertical(vert);
        }, (v) -> {
        });
        horizontalMove.start();
        verticalMove.start();
        SolarPanelMover mover = new SolarPanelMover(() -> updatedPosition.get(), horizontalMove, verticalMove);
        mover.moveTo("1", position(5, 0));
        Thread.sleep(200);
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 0, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD, true));
    }

    @Test
    public void moveToTwice() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 10);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);

        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>(position(0, 0));
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        HorizontalMove horizontalMove = new HorizontalMove(horizontalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int hor = updatedPosition.get().getHorizontal() + tick;
            updatedPosition.get().setHorizontal(hor);
        }, (v) -> {
        });
        VerticalMove verticalMove = new VerticalMove(verticalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int vert = updatedPosition.get().getVertical() + tick;
            updatedPosition.get().setVertical(vert);
        }, (v) -> {
        });
        horizontalMove.start();
        verticalMove.start();
        SolarPanelMover mover = new SolarPanelMover(() -> updatedPosition.get(), horizontalMove, verticalMove);
        mover.moveTo("1", position(5, 0));
        Thread.sleep(200);
        mover.waitForEnd();
        System.out.println(updatedPosition.get());
        mover.moveTo("2", position(5, 5));
        Thread.sleep(200);
        mover.waitForEnd();
        Assert.assertEquals("We shouldnt move", 5, updatedPosition.get().getVertical());
        Assert.assertEquals("We should move 5 ticks to east", 5, updatedPosition.get().getHorizontal());
        Assert.assertTrue(mockedPinManager.getState(SolarSystemRefCode.EAST_PIN_REF_CD).isLow());
        Assert.assertTrue(mockedPinManager.wasState(SolarSystemRefCode.EAST_PIN_REF_CD, true));
    }

    @Test
    public void lessTicksThanExpected() throws InterruptedException {
        TickerPin verticalTickPin = new TickerPin(100, 5);
        TickerPin horizontalTickPin = new TickerPin(100, 3);

        VerticalMoveFeedback verticalMoveFeedback = new VerticalMoveFeedback();
        verticalMoveFeedback.start(verticalTickPin);
        HorizontalMoveFeedback horizontalMoveFeedback = new HorizontalMoveFeedback();
        horizontalMoveFeedback.start(horizontalTickPin);


        AtomicReference<AbsolutePosition> updatedPosition = new AtomicReference<>(position(0, 0));
        SolarMockedPinManager mockedPinManager = new SolarMockedPinManager(verticalTickPin, horizontalTickPin);
        HorizontalMove horizontalMove = new HorizontalMove(horizontalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int hor = updatedPosition.get().getHorizontal() + tick;
            updatedPosition.get().setHorizontal(hor);
        }, (v) -> {
        });
        VerticalMove verticalMove = new VerticalMove(verticalMoveFeedback, Logger.getLogger("Solar"), mockedPinManager, tick -> {
            int vert = updatedPosition.get().getVertical() + tick;
            updatedPosition.get().setVertical(vert);
        }, (v) -> {
        });
        horizontalMove.start();
        verticalMove.start();
        SolarPanelMover mover = new SolarPanelMover(() -> updatedPosition.get(), horizontalMove, verticalMove);
        mover.moveTo("1", position(5, 0));
        Thread.sleep(200);
        mover.waitForEnd();
        Thread.sleep(3200);
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
