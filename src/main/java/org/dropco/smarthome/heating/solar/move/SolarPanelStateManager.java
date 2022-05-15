package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.dropco.smarthome.TimeUtil;
import org.dropco.smarthome.heating.solar.ServiceMode;
import org.dropco.smarthome.heating.solar.SolarSerializer;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.ParkPosition;
import org.dropco.smarthome.heating.solar.dto.Position;
import org.dropco.smarthome.heating.solar.dto.PositionProcessor;
import org.dropco.smarthome.heating.solar.dto.SolarPanelStep;
import org.dropco.smarthome.heating.solar.dto.SolarSchedule;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private AtomicBoolean beforeFirstMove = new AtomicBoolean(true);
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
        if (event == Event.WARM_WATER) {
          if (beforeFirstMove.get())   add(event, true);
        } else
            add(event, true);

    }

    public void add(Event event, boolean emitEvents) {
        LOGGER.log(Level.FINE, "Adding event " + event + " to " + currentEvents + ". Emit=" + emitEvents);
        if (currentEvents.add(event)) {
            updateEvents();
            switch (event) {
                case STRONG_WIND:
                    if (emitEvents) strongWind();
                    break;
                case PANEL_OVERHEATED:
                case WATER_OVERHEATED:
                case SOLAR_PUMP_MALFUNCTION:
                    if (emitEvents) runAwayFromSunPosition();
                    break;
                case DAY_LIGHT_REACHED:
                    if (emitEvents) nextTick();
                    break;
                case PARKING_POSITION:
                    if (emitEvents && !ServiceMode.isServiceMode()) {
                        mover.moveTo("PARKING_POSITION", Movement.WEST, Movement.NORTH);
                    }
                default:
            }

        }
    }

    public void remove(Event event) {
        LOGGER.log(Level.FINE, "Removing event " + event + " from " + currentEvents);
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
                case SOLAR_PUMP_MALFUNCTION:
                    nextTick();
                    break;
                default:
            }
        }
    }

    public void nextTick() {
        if (!currentEvents.contains(Event.PARKING_POSITION)) {
            if (!(currentEvents.contains(Event.PANEL_OVERHEATED) || currentEvents.contains(Event.WATER_OVERHEATED) || currentEvents.contains(Event.SOLAR_PUMP_MALFUNCTION))) {
                if (!ServiceMode.isServiceMode())
                    calculatePosition().ifPresent(step -> {
                        if (step.getPosition() != null) {
                            beforeFirstMove.set(false);
                            if (step.getPosition() instanceof ParkPosition) {
                                add(Event.PARKING_POSITION);
                            } else {
                                if (currentEvents.contains(Event.WARM_WATER))
                                    mover.moveTo("WARM_WATER", null, Movement.SOUTH);
                                else
                                    mover.moveTo(step.getHour() + ":" + step.getMinute(), step.getPosition());
                            }
                        }
                    });
            } else {
                runAwayFromSunPosition();
            }
        }
    }

    private void runAwayFromSunPosition() {
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
                        while (count[0] > 0 && deltaPos.getVerticalCount() < 0) {
                            deltaPos.setVerticalCount(deltaPos.getVerticalCount() + 1);
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
        SolarSchedule solarSchedule = todaysSchedule.get();
        Iterator<SolarPanelStep> steps = solarSchedule.getSteps().iterator();
        Record record = new Record();
        Calendar currentTime = getCurrentTime();
        while (steps.hasNext()) {
            SolarPanelStep step = steps.next();
            if (!TimeUtil.isAfter(currentTime, step.getHour(), step.getMinute())) {
                if (step.getIgnoreDayLight() || currentEvents.contains(Event.DAY_LIGHT_REACHED)) {
                    record.setHour(step.getHour());
                    record.setMinute(step.getMinute());
                    record.setPosition(merge(record.getPosition(), step.getPosition(), solarSchedule.getHorizontalTickCountForStep(), solarSchedule.getVerticalTickCountForStep()));
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

    private Position merge(Position prev, Position current, int horizontalTickCountForStep, int verticalTickCountForStep) {
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
                        return new AbsolutePosition(prevAbsPos.getHorizontal() + (deltaPos.getHorizontalCount() * horizontalTickCountForStep), prevAbsPos.getVertical() + (deltaPos.getVerticalCount() * verticalTickCountForStep));
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
                        return new DeltaPosition(prevDeltaPos.getHorizontalCount() + deltaPos.getHorizontalCount(), prevDeltaPos.getVerticalCount() + deltaPos.getVerticalCount());
                    }
                });
            }
        });
    }


    public void dailyReset() {
        currentEvents.clear();
        updateEvents();
        beforeFirstMove.set(true);
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
        WARM_WATER,
        WATER_OVERHEATED,
        PANEL_OVERHEATED,
        DAY_LIGHT_REACHED,
        PARKING_POSITION,
        SOLAR_PUMP_MALFUNCTION;
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
