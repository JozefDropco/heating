package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.TimeUtil;
import org.dropco.smarthome.heating.solar.SolarSerializer;
import org.dropco.smarthome.heating.solar.dto.*;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SolarPanelStateManager {
    private String afternoonTime;
    private int SOUTH = 430;
    private int NORTH = 0;
    private int WEST = 0;
    private int EAST = 690;

    private Supplier<AbsolutePosition> currentPosition;
    private final Mover mover;

    private SolarSchedule todaysSchedule;
    private Supplier<SolarSchedule> solarScheduleSupplier;
    private Consumer<String> recentSolarScheduleUpdater;

    private Set<Event> currentEvents = Sets.newHashSet();
    private Consumer<String> currentEventsUpdater;

    public SolarPanelStateManager(String afternoonTime, int south, int north, int west, int east,
                                  Supplier<AbsolutePosition> currentPosition, Mover mover,
                                  Supplier<String> recentTodaysSchedule, Supplier<SolarSchedule> solarScheduleSupplier, Consumer<String> recentSolarScheduleUpdater,
                                  Supplier<String> currentEventsSupplier, Consumer<String> currentEventsUpdater) {
        this.afternoonTime = afternoonTime;
        this.SOUTH = south;
        this.NORTH = north;
        this.WEST = west;
        this.EAST = east;
        this.solarScheduleSupplier = solarScheduleSupplier;
        this.mover = mover;
        this.recentSolarScheduleUpdater = recentSolarScheduleUpdater;
        this.currentPosition = currentPosition;
        this.currentEventsUpdater = currentEventsUpdater;
        String json = recentTodaysSchedule.get();
        if (json == null) {
            todaysSchedule = solarScheduleSupplier.get();
            updateSchedule();
        } else {
            todaysSchedule = SolarSerializer.getGson().fromJson(json, SolarSchedule.class);
            if (!TimeUtil.isToday(todaysSchedule.getAsOfDate())) {
                todaysSchedule = solarScheduleSupplier.get();
                updateSchedule();
            }
        }
        String serializedEvents = currentEventsSupplier.get();
        if (serializedEvents != null) {
            currentEvents = SolarSerializer.getGson().fromJson(serializedEvents, new TypeToken<Set<Event>>() {
            }.getType());
        }
    }

    public void add(Event event) {
        if (currentEvents.add(event)) {
            updateEvents();
            switch (event) {
                case STRONG_WIND:
                    strongWind();
                    break;
                case PANEL_OVERHEATED:
                case WATER_OVERHEATED:
                    overheated();
                    break;
                case DAY_LIGHT_REACHED:
                    nextTick();
                    break;
                case PARKING_POSTION:
                    if (!ServiceMode.isServiceMode())
                        mover.moveTo("PARKING_POSITION", new AbsolutePosition(WEST, NORTH));
            }

        }
    }

    public void remove(Event event) {
        if (currentEvents.remove(event)) {
            updateEvents();
            switch (event) {
                case STRONG_WIND:
                    if (!ServiceMode.isServiceMode())
                        mover.moveTo("noWind", new DeltaPosition(0, 0));
                    break;
                case PARKING_POSTION:
                case PANEL_OVERHEATED:
                case WATER_OVERHEATED:
                    nextTick();
                    break;
                default:
            }
        }
    }

    public void nextTick() {
        if (!currentEvents.contains(Event.PARKING_POSTION)) {
            if (!(currentEvents.contains(Event.PANEL_OVERHEATED) || currentEvents.contains(Event.WATER_OVERHEATED))) {
                if (!ServiceMode.isServiceMode())
                    calculatePosition().ifPresent(step -> mover.moveTo(step.getHour() + ":" + step.getMinute(), step.getPosition()));
            } else {
                overheated();
            }
        }
    }

    private void overheated() {
        if (!ServiceMode.isServiceMode())
            if (!currentEvents.contains(Event.PARKING_POSTION)) {
            AbsolutePosition current = currentPosition.get();

            if (TimeUtil.isAfternoon(getCurrentTime(), afternoonTime)) {
                mover.moveTo("overheated_afternoon", new AbsolutePosition(EAST, (currentEvents.contains(Event.STRONG_WIND) ? current.getVertical() : SOUTH)));
            } else {
                mover.moveTo("overheated_morning", new AbsolutePosition(WEST, (currentEvents.contains(Event.STRONG_WIND) ? current.getVertical() : SOUTH)));
            }
        }
    }

    private void strongWind() {
        final int[] count = {2};
        todaysSchedule.getSteps().forEach(step -> {
            PositionProcessor<Void> updateFirstTwoRecords = new PositionProcessor<Void>() {

                @Override
                public Void process(DeltaPosition deltaPos) {
                    if (TimeUtil.isAfter(getCurrentTime(), step.getHour(), step.getMinute()))
                        if (count[0] > 0 && deltaPos.getDeltaVerticalTicks() < 0) {
                            deltaPos.setDeltaVerticalTicks(0);
                            count[0]--;
                        }
                    return null;
                }
            };
            step.getPosition().invoke(updateFirstTwoRecords);
        });
        updateSchedule();
        if (!ServiceMode.isServiceMode())
            mover.moveTo("strongWind", new DeltaPosition(0, -2 * todaysSchedule.getVerticalTickCountForStep()));
    }

    protected Calendar getCurrentTime() {
        return Calendar.getInstance();
    }


    private void updateEvents() {
        currentEventsUpdater.accept(SolarSerializer.getGson().toJson(currentEvents));
    }

    public Optional<Record> calculatePosition() {
        Iterator<SolarPanelStep> steps = todaysSchedule.getSteps().iterator();
        Record record = new Record();
        while (steps.hasNext()) {
            SolarPanelStep step = steps.next();
            if (!TimeUtil.isAfter(getCurrentTime(), step.getHour(), step.getMinute())) {
                if (step.getIgnoreDayLight() || currentEvents.contains(Event.DAY_LIGHT_REACHED)) {
                    record.setHour(step.getHour());
                    record.setMinute(step.getMinute());
                    record.setPosition(merge(record.getPosition(), step.getPosition()));
                }
            } else {
                record.setNextMoveHour(step.getHour());
                record.setNextMoveMinute(step.getMinute());
                return Optional.ofNullable(record);
            }
        }
        return Optional.ofNullable(record);
    }

    private Position merge(Position prev, Position current) {
        if (prev == null) return current;
        return prev.invoke(new PositionProcessor<Position>() {
            @Override
            public Position process(AbsolutePosition prevAbsPos) {
                return current.invoke(new PositionProcessor<Position>() {
                    @Override
                    public Position process(AbsolutePosition absPos) {
                        return absPos;
                    }

                    @Override
                    public Position process(DeltaPosition deltaPos) {
                        return new AbsolutePosition(prevAbsPos.getHorizontal() + deltaPos.getDeltaHorizontalTicks(), prevAbsPos.getVertical() + deltaPos.getDeltaVerticalTicks());
                    }
                });
            }

            @Override
            public Position process(DeltaPosition prevDeltaPos) {
                return current.invoke(new PositionProcessor<Position>() {
                    @Override
                    public Position process(AbsolutePosition absPos) {
                        return absPos;
                    }

                    @Override
                    public Position process(DeltaPosition deltaPos) {
                        return new DeltaPosition(prevDeltaPos.getDeltaHorizontalTicks() + deltaPos.getDeltaHorizontalTicks(), prevDeltaPos.getDeltaVerticalTicks() + deltaPos.getDeltaVerticalTicks());
                    }
                });
            }
        });
    }


    public void dailyReset() {
        currentEvents.clear();
        updateEvents();
        todaysSchedule = solarScheduleSupplier.get();
        updateSchedule();
    }

    private void updateSchedule() {
        recentSolarScheduleUpdater.accept(SolarSerializer.getGson().toJson(todaysSchedule));
    }

    public boolean has(Event event) {
        return currentEvents.contains(event);
    }


    public enum Event {
        STRONG_WIND,
        WATER_OVERHEATED,
        PANEL_OVERHEATED,
        DAY_LIGHT_REACHED,
        PARKING_POSTION;
    }

    public static class Record {
        private Position position;
        private Integer nextMoveHour;
        private Integer nextMoveMinute;
        private int hour;
        private int minute;

        /***
         * Gets the position
         * @return
         */
        public Position getPosition() {
            return position;
        }

        public Record setPosition(Position position) {
            this.position = position;
            return this;
        }

        /***
         * Gets the nextMoveHour
         * @return
         */
        public Integer getNextMoveHour() {
            return nextMoveHour;
        }

        public Record setNextMoveHour(Integer nextMoveHour) {
            this.nextMoveHour = nextMoveHour;
            return this;
        }

        /***
         * Gets the nextMoveMinute
         * @return
         */
        public Integer getNextMoveMinute() {
            return nextMoveMinute;
        }

        public Record setNextMoveMinute(Integer nextMoveMinute) {
            this.nextMoveMinute = nextMoveMinute;
            return this;
        }

        /***
         * Gets the hour
         * @return
         */
        public int getHour() {
            return hour;
        }

        public Record setHour(int hour) {
            this.hour = hour;
            return this;
        }

        /***
         * Gets the minute
         * @return
         */
        public int getMinute() {
            return minute;
        }

        public Record setMinute(int minute) {
            this.minute = minute;
            return this;
        }
    }
}
