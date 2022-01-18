package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.dropco.smarthome.TimeUtil;
import org.dropco.smarthome.heating.solar.SolarSerializer;
import org.dropco.smarthome.heating.solar.dto.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.Set;
import java.util.function.Consumer;

public class SolarPanelStateManagerTest {

    public static final String AFTERNOON_TIME = "13:10";
    @Test
    public void daylightReached() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(0, 0);
        Consumer<String> recentSolarScheduleUpdater = (Consumer<String>) s -> {
            System.out.println("SolarSchedule: " + s);
        };
        Consumer<String> currentEventsUpdater = new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("Events :" + s);
            }
        };
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager(AFTERNOON_TIME,  mover, () -> null, () -> solarSchedule, recentSolarScheduleUpdater, () -> "[]", currentEventsUpdater
        ){
            @Override
            protected Calendar getCurrentTime() {
                Calendar currentTime = super.getCurrentTime();
                currentTime.set(Calendar.HOUR_OF_DAY, 8);
                currentTime.set(Calendar.MINUTE, 20);
                return currentTime;
            }
        };
        manager.calculatePosition();
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                Assert.assertEquals(8, absPos.getHorizontal());
                Assert.assertEquals(8, absPos.getVertical());
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.fail();
                return null;
            }
        });
    }
    @Test
    public void strongWind() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer recentSolarUpdater = Mockito.mock(Consumer.class);
        Consumer currentEventsUpdater = Mockito.mock(Consumer.class);
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager("13:10", mover, () -> null, () -> solarSchedule, recentSolarUpdater, () -> null, currentEventsUpdater
        ) {
            @Override
            protected Calendar getCurrentTime() {
                Calendar currentTime = super.getCurrentTime();
                currentTime.set(Calendar.HOUR_OF_DAY, 4);
                return currentTime;
            }
        };
        ArgumentCaptor<String> solarUpdaterCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doAnswer((invocation) -> {
            System.out.println(invocation.getArgument(0).toString());
            return null;
        }).when(recentSolarUpdater).accept(solarUpdaterCaptor.capture());
        ArgumentCaptor<String> currentEventsUpdaterCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doAnswer((invocation) -> {
            System.out.println(invocation.getArgument(0).toString());
            return null;
        }).when(currentEventsUpdater).accept(currentEventsUpdaterCaptor.capture());

        manager.add(SolarPanelStateManager.Event.STRONG_WIND);
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                Assert.fail();
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.assertEquals(-2 * solarSchedule.getVerticalTickCountForStep(), deltaPos.getDeltaVerticalTicks());
                Assert.assertEquals(0, deltaPos.getDeltaHorizontalTicks());
                return null;
            }
        });
        Gson gson = SolarSerializer.getGson();
        SolarSchedule updatedSchedule = gson.fromJson(solarUpdaterCaptor.getValue(), SolarSchedule.class);
        Set<SolarPanelStateManager.Event> events = gson.fromJson(currentEventsUpdaterCaptor.getValue(), new TypeToken<Set<SolarPanelStateManager.Event>>() {
        }.getType());
        Assert.assertTrue(events.contains(SolarPanelStateManager.Event.STRONG_WIND));
        DeltaPosition deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(1).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(2).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(3).getPosition();
        Assert.assertEquals(-1, deltaPosition.getDeltaVerticalTicks());
    }

    @Test
    public void strongWindGone() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer<String> recentSolarScheduleUpdater = (Consumer<String>) s -> {
            System.out.println("SolarSchedule: " + s);
        };
        Consumer<String> currentEventsUpdater = new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("Events :" + s);
            }
        };
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager("13:10", mover, () -> null, () -> solarSchedule, recentSolarScheduleUpdater, () -> "[\"STRONG_WIND\"]", currentEventsUpdater
        );
        manager.remove(SolarPanelStateManager.Event.STRONG_WIND);
        Assert.assertNull(mover.lastPosition);
    }

    @Test
    public void overheated() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer<String> recentSolarScheduleUpdater = (Consumer<String>) s -> {
            System.out.println("SolarSchedule: " + s);
        };
        Consumer<String> currentEventsUpdater = new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("Events :" + s);
            }
        };
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager(AFTERNOON_TIME, mover, () -> null, () -> solarSchedule, recentSolarScheduleUpdater, () -> null, currentEventsUpdater
        );
        manager.add(SolarPanelStateManager.Event.PANEL_OVERHEATED);
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                if (TimeUtil.isAfternoon(Calendar.getInstance(), AFTERNOON_TIME)) {
                    Assert.assertEquals(10, absPos.getHorizontal());
                } else
                    Assert.assertEquals(0, absPos.getHorizontal());
                Assert.assertEquals(10, absPos.getVertical());
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.fail();
                return null;
            }
        });
    }

    @Test
    public void cooledDown() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer<String> recentSolarScheduleUpdater = (Consumer<String>) s -> {
            System.out.println("SolarSchedule: " + s);
        };
        Consumer<String> currentEventsUpdater = new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("Events :" + s);
            }
        };
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager(AFTERNOON_TIME, mover, () -> null, () -> solarSchedule, recentSolarScheduleUpdater, () -> "[\"PANEL_OVERHEATED\"]", currentEventsUpdater
        );
        manager.remove(SolarPanelStateManager.Event.PANEL_OVERHEATED);
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                Assert.assertEquals(10, absPos.getHorizontal());
                Assert.assertEquals(10, absPos.getVertical());
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.fail();
                return null;
            }
        });
    }

    @Test
    public void cooledDownAfterSun() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer<String> recentSolarScheduleUpdater = (Consumer<String>) s -> {
            System.out.println("SolarSchedule: " + s);
        };
        Consumer<String> currentEventsUpdater = new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("Events :" + s);
            }
        };
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager(AFTERNOON_TIME, mover, () -> null, () -> solarSchedule, recentSolarScheduleUpdater, () -> "[\"PANEL_OVERHEATED\",\"DAY_LIGHT_REACHED\"]", currentEventsUpdater
        ){
            @Override
            protected Calendar getCurrentTime() {
                Calendar currentTime = super.getCurrentTime();
                currentTime.set(Calendar.HOUR_OF_DAY,15);
                return currentTime;
            }
        };
        manager.remove(SolarPanelStateManager.Event.PANEL_OVERHEATED);
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                Assert.assertEquals(6, absPos.getHorizontal());
                Assert.assertEquals(6, absPos.getVertical());
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.fail();
                return null;
            }
        });
    }

    @Test
    public void strongWindAfterStrongWind_noAction() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer recentSolarUpdater = Mockito.mock(Consumer.class);
        Consumer currentEventsUpdater = Mockito.mock(Consumer.class);
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager("13:10", mover, () -> null, () -> solarSchedule, recentSolarUpdater, () -> null, currentEventsUpdater
        ) {
            @Override
            protected Calendar getCurrentTime() {
                Calendar currentTime = super.getCurrentTime();
                currentTime.set(Calendar.HOUR_OF_DAY, 4);
                return currentTime;
            }
        };
        ArgumentCaptor<String> solarUpdaterCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doAnswer((invocation) -> {
            System.out.println(invocation.getArgument(0).toString());
            return null;
        }).when(recentSolarUpdater).accept(solarUpdaterCaptor.capture());
        ArgumentCaptor<String> currentEventsUpdaterCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doAnswer((invocation) -> {
            System.out.println(invocation.getArgument(0).toString());
            return null;
        }).when(currentEventsUpdater).accept(currentEventsUpdaterCaptor.capture());

        manager.add(SolarPanelStateManager.Event.STRONG_WIND);
        manager.add(SolarPanelStateManager.Event.STRONG_WIND);
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                Assert.fail();
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.assertEquals(-2 * solarSchedule.getVerticalTickCountForStep(), deltaPos.getDeltaVerticalTicks());
                Assert.assertEquals(0, deltaPos.getDeltaHorizontalTicks());
                return null;
            }
        });
        Gson gson = SolarSerializer.getGson();
        SolarSchedule updatedSchedule = gson.fromJson(solarUpdaterCaptor.getValue(), SolarSchedule.class);
        Set<SolarPanelStateManager.Event> events = gson.fromJson(currentEventsUpdaterCaptor.getValue(), new TypeToken<Set<SolarPanelStateManager.Event>>() {
        }.getType());
        Assert.assertTrue(events.contains(SolarPanelStateManager.Event.STRONG_WIND));
        DeltaPosition deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(1).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(2).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(3).getPosition();
        Assert.assertEquals(-1, deltaPosition.getDeltaVerticalTicks());
    }

    @Test
    public void strongWindAfterStrongWind() {
        MockedMover mover = new MockedMover();
        AbsolutePosition current = new AbsolutePosition(10, 10);
        Consumer recentSolarUpdater = Mockito.mock(Consumer.class);
        Consumer currentEventsUpdater = Mockito.mock(Consumer.class);
        SolarSchedule solarSchedule = getSolarSchedule();
        SolarPanelStateManager manager = new SolarPanelStateManager("13:10", mover, () -> null, () -> solarSchedule, recentSolarUpdater, () -> null, currentEventsUpdater
        ) {
            @Override
            protected Calendar getCurrentTime() {
                Calendar currentTime = super.getCurrentTime();
                currentTime.set(Calendar.HOUR_OF_DAY, 4);
                return currentTime;
            }
        };
        ArgumentCaptor<String> solarUpdaterCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doAnswer((invocation) -> {
            System.out.println(invocation.getArgument(0).toString());
            return null;
        }).when(recentSolarUpdater).accept(solarUpdaterCaptor.capture());
        ArgumentCaptor<String> currentEventsUpdaterCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doAnswer((invocation) -> {
            System.out.println(invocation.getArgument(0).toString());
            return null;
        }).when(currentEventsUpdater).accept(currentEventsUpdaterCaptor.capture());

        manager.add(SolarPanelStateManager.Event.STRONG_WIND);
        manager.remove(SolarPanelStateManager.Event.STRONG_WIND);
        manager.add(SolarPanelStateManager.Event.STRONG_WIND);
        mover.lastPosition.invoke(new PositionProcessor<Object>() {
            @Override
            public Object process(AbsolutePosition absPos) {
                Assert.fail();
                return null;
            }

            @Override
            public Object process(DeltaPosition deltaPos) {
                Assert.assertEquals(-2 * solarSchedule.getVerticalTickCountForStep(), deltaPos.getDeltaVerticalTicks());
                Assert.assertEquals(0, deltaPos.getDeltaHorizontalTicks());
                return null;
            }
        });
        Gson gson = SolarSerializer.getGson();
        SolarSchedule updatedSchedule = gson.fromJson(solarUpdaterCaptor.getValue(), SolarSchedule.class);
        Set<SolarPanelStateManager.Event> events = gson.fromJson(currentEventsUpdaterCaptor.getValue(), new TypeToken<Set<SolarPanelStateManager.Event>>() {
        }.getType());
        Assert.assertTrue(events.contains(SolarPanelStateManager.Event.STRONG_WIND));
        DeltaPosition deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(1).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(2).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(3).getPosition();
        Assert.assertEquals(0, deltaPosition.getDeltaVerticalTicks());
        deltaPosition = (DeltaPosition) updatedSchedule.getSteps().get(4).getPosition();
        Assert.assertEquals(1, deltaPosition.getDeltaVerticalTicks());
    }

    private SolarSchedule getSolarSchedule() {
        SolarSchedule solarSchedule = new SolarSchedule();
        solarSchedule.setMonth(1);
        solarSchedule.setHorizontalTickCountForStep(0);
        solarSchedule.setVerticalTickCountForStep(2);
        solarSchedule.setSteps(Lists.newArrayList());
        solarSchedule.getSteps().add(solarStep(6, 30, false, absPosition(10, 10)));
        solarSchedule.getSteps().add(solarStep(8, 30, false, deltaPosition(-2, -2)));
        solarSchedule.getSteps().add(solarStep(10, 30, false, deltaPosition(-2, -2)));
        solarSchedule.getSteps().add(solarStep(12, 30, false, deltaPosition(-1, -1)));
        solarSchedule.getSteps().add(solarStep(14, 30, false, deltaPosition(1, 1)));
        solarSchedule.getSteps().add(solarStep(16, 30, false, deltaPosition(2, 2)));
        solarSchedule.getSteps().add(solarStep(18, 30, false, deltaPosition(2, 2)));
        solarSchedule.getSteps().add(solarStep(20, 30, false, absPosition(0, 0)));
        return solarSchedule;
    }

    private Position absPosition(int hor, int vert) {
        return new AbsolutePosition(hor, vert);
    }

    private Position deltaPosition(int hor, int vert) {
        return new DeltaPosition(hor, vert);
    }

    private SolarPanelStep solarStep(int hour, int minute, boolean ignoreDayLight, Position position) {
        SolarPanelStep step = new SolarPanelStep();
        step.setHour(hour);
        step.setMinute(minute);
        step.setIgnoreDayLight(ignoreDayLight);
        step.setPosition(position);
        return step;
    }

    private class MockedMover implements Mover {
        Position lastPosition;

        @Override
        public void moveTo(String movementRefCd, Position position) {
            lastPosition = position;
        }

        @Override
        public void stop() {
            System.out.println("STOP mover");
        }

        @Override
        public void moveTo(String movementRefCd, Movement horizontal, Movement vertical) {
            System.out.println("Move to ref-cd="+movementRefCd+" "+horizontal.getPinRefCd()+", " +vertical.getPinRefCd());
        }

        @Override
        public void moveTo(Movement movement, boolean state) {
            System.out.println("Move to mover - "+movement.getPinRefCd()+", state="+state);
        }
    }
}
