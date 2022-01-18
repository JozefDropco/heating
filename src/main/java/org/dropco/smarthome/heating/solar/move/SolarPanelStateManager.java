package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.dropco.smarthome.TimeUtil;
import org.dropco.smarthome.heating.solar.ServiceMode;
import org.dropco.smarthome.heating.solar.SolarSerializer;
import org.dropco.smarthome.heating.solar.dto.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarPanelStateManager {
    public static final Logger LOGGER = Logger.getLogger(SolarPanelStateManager.class.getName());
    private final Mover mover;
    private final Set<Event> currentEvents = Collections.synchronizedSet(Sets.newHashSet());
    private String afternoonTime;
    private AtomicReference<SolarSchedule> todaysSchedule = new AtomicReference<>();
    private Supplier<SolarSchedule> solarScheduleSupplier;
    private Consumer<String> recentSolarScheduleUpdater;
    private Consumer<String> currentEventsUpdater;

    public SolarPanelStateManager(String afternoonTime,
                                  Mover mover,
                                  Supplier<String> recentTodaysSchedule, Supplier<SolarSchedule> solarScheduleSupplier, Consumer<String> recentSolarScheduleUpdater,
                                  Supplier<String> currentEventsSupplier, Consumer<String> currentEventsUpdater) {
        this.afternoonTime = afternoonTime;

        this.solarScheduleSupplier = solarScheduleSupplier;
        this.mover = mover;
        this.recentSolarScheduleUpdater = recentSolarScheduleUpdater;
        this.currentEventsUpdater = currentEventsUpdater;
        String json = recentTodaysSchedule.get();
        if (json == null) {
            todaysSchedule.set(solarScheduleSupplier.get());
            updateSchedule();
        } else {
            todaysSchedule.set(SolarSerializer.getGson().fromJson(json, SolarSchedule.class));
            if (!TimeUtil.isToday(todaysSchedule.get().getAsOfDate())) {
                todaysSchedule.set(solarScheduleSupplier.get());
                updateSchedule();
            }
        }
        String serializedEvents = currentEventsSupplier.get();
        if (serializedEvents != null) {
            currentEvents.addAll(SolarSerializer.getGson().fromJson(serializedEvents, new TypeToken<Set<Event>>() {
            }.getType()));
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
                case PARKING_POSITION:
                    if (!ServiceMode.isServiceMode()) {
                        mover.moveTo("PARKING_POSITION", Movement.WEST, Movement.NORTH);
                    }
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
                case PARKING_POSITION:
                case PANEL_OVERHEATED:
                case WATER_OVERHEATED:
                    nextTick();
                    break;
                default:
            }
        }
    }

    public void nextTick() {
        if (!currentEvents.contains(Event.PARKING_POSITION)) {
            if (!(currentEvents.contains(Event.PANEL_OVERHEATED) || currentEvents.contains(Event.WATER_OVERHEATED))) {
                if (!ServiceMode.isServiceMode())
                    calculatePosition().ifPresent(step -> {
                        if (step.getPosition() != null) {
                            if (step.getPosition() instanceof ParkPosition) {
                                mover.moveTo(step.getHour() + ":" + step.getMinute(), Movement.WEST,Movement.NORTH);
                            } else
                                mover.moveTo(step.getHour() + ":" + step.getMinute(), step.getPosition());
                        }
                    });
            } else {
                overheated();
            }
        }
    }

    private void overheated() {
        if (!ServiceMode.isServiceMode())
            if (!currentEvents.contains(Event.PARKING_POSITION)) {
                if (TimeUtil.isAfternoon(getCurrentTime(), afternoonTime)) {
                    mover.moveTo("overheated_afternoon", Movement.EAST, currentEvents.contains(Event.STRONG_WIND) ? null : Movement.SOUTH);
                } else {
                    mover.moveTo("overheated_morning", Movement.WEST, currentEvents.contains(Event.STRONG_WIND) ? null : Movement.SOUTH);
                }
            }
    }

    private void strongWind() {
        final int[] count = {2};
        todaysSchedule.get().getSteps().forEach(step -> {
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
            mover.moveTo("strongWind", new DeltaPosition(0, -2 * todaysSchedule.get().getVerticalTickCountForStep()));
    }

    protected Calendar getCurrentTime() {
        return Calendar.getInstance();
    }


    private void updateEvents() {
        currentEventsUpdater.accept(SolarSerializer.getGson().toJson(currentEvents));
    }

    public Optional<Record> calculatePosition() {
        Iterator<SolarPanelStep> steps = todaysSchedule.get().getSteps().iterator();
        Record record = new Record();
        Calendar currentTime = getCurrentTime();
        while (steps.hasNext()) {
            SolarPanelStep step = steps.next();
            if (!TimeUtil.isAfter(currentTime, step.getHour(), step.getMinute())) {
                if (step.getIgnoreDayLight() || currentEvents.contains(Event.DAY_LIGHT_REACHED)) {
                    record.setHour(step.getHour());
                    record.setMinute(step.getMinute());
                    record.setPosition(merge(record.getPosition(), step.getPosition()));
                }
            } else {
                record.setNextMoveHour(step.getHour());
                record.setNextMoveMinute(step.getMinute());
                LOGGER.log(Level.CONFIG, "Calculated solar panel records: " + record);
                return Optional.ofNullable(record);
            }
        }
        LOGGER.log(Level.CONFIG, "Calculated solar panel records: " + record);
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

                    @Override
                    public Position process(ParkPosition parkPosition) {
                        return parkPosition;
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
        todaysSchedule.set(solarScheduleSupplier.get());
        updateSchedule();
    }

    private void updateSchedule() {
        recentSolarScheduleUpdater.accept(SolarSerializer.getGson().toJson(todaysSchedule.get()));
    }

    public boolean has(Event event) {
        return currentEvents.contains(event);
    }

    public synchronized Set<String> move(Movement movement, Boolean state) {
        mover.moveTo(movement, state);
        switch (movement) {
            case SOUTH:
                return Collections.singleton(Movement.NORTH.getPinRefCd());
            case NORTH:
                return Collections.singleton(Movement.SOUTH.getPinRefCd());
            case WEST:
                return Collections.singleton(Movement.EAST.getPinRefCd());
            case EAST:
                return Collections.singleton(Movement.WEST.getPinRefCd());
            default:
                return null;
        }
    }


    public enum Event {
        STRONG_WIND,
        WATER_OVERHEATED,
        PANEL_OVERHEATED,
        DAY_LIGHT_REACHED,
        PARKING_POSITION;
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

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Record{");
            sb.append("position=").append(position);
            sb.append(", nextMoveHour=").append(nextMoveHour);
            sb.append(", nextMoveMinute=").append(nextMoveMinute);
            sb.append(", hour=").append(hour);
            sb.append(", minute=").append(minute);
            sb.append('}');
            return sb.toString();
        }
    }
}
