package org.dropco.smarthome.solar;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

@Ignore
public class SolarSystemGenTest {
    int index = 4;
    List<Clock> clocks = Lists.newArrayList();
    private Multimap<Schedule, Position> positionMultimap;


    @Before
    public void setup() {
        positionMultimap = Multimaps.newMultimap(new HashMap<>(), ArrayList::new);
        monthly(positionMultimap, new Schedule(1, 6, 30), new Position(-135, 0));
        monthly(positionMultimap, new Schedule(1, 11, 40), new Position(null, -35));
        monthly(positionMultimap, new Schedule(1, 12, 10), new Position(null, -70));
        monthly(positionMultimap, new Schedule(1, 12, 40), new Position(null, -105));
        monthly(positionMultimap, new Schedule(1, 13, 10), new Position(null, -140));
        monthly(positionMultimap, new Schedule(1, 13, 40), new Position(null, -175));
        monthly(positionMultimap, new Schedule(1, 14, 10), new Position(null, -210));
        monthly(positionMultimap, new Schedule(1, 14, 40), new Position(null, -245));
        monthly(positionMultimap, new Schedule(1, 15, 10), new Position(null, -280));
        put(positionMultimap, new Schedule(1, 21, 0), new Position(0, null), 3, 4, 5, 6, 7, 8, 9, 10);
        put(positionMultimap, new Schedule(1, 17, 10), new Position(0, null), 11, 12, 1, 2);
        put(positionMultimap, new Schedule(1, 7, 10), new Position(-135, null), 11, 12, 1);
        put(positionMultimap, new Schedule(1, 8, 10), new Position(-135, null), 11, 12, 1);
        put(positionMultimap, new Schedule(1, 9, 10), new Position(-135, null), 11, 12, 1);
        put(positionMultimap, new Schedule(1, 10, 10), new Position(-135, null), 11, 12, 1);
        put(positionMultimap, new Schedule(1, 11, 10), new Position(-135, null), 11, 12, 1);

        put(positionMultimap, new Schedule(1, 10, 10), new Position(-125, null), 2, 10);
        put(positionMultimap, new Schedule(1, 11, 10), new Position(-114, null), 2, 10);
        put(positionMultimap, new Schedule(1, 12, 10), new Position(-105, null), 2, 10);
        put(positionMultimap, new Schedule(1, 13, 10), new Position(-96, null), 2, 10);
        put(positionMultimap, new Schedule(1, 14, 10), new Position(-105, null), 2, 10);
        put(positionMultimap, new Schedule(1, 15, 10), new Position(-114, null), 2, 10);
        put(positionMultimap, new Schedule(1, 16, 10), new Position(-125, null), 2, 10);
        put(positionMultimap, new Schedule(1, 17, 10), new Position(-135, null), 10);

        put(positionMultimap, new Schedule(1, 8, 10), new Position(-125, null), 3, 9);
        put(positionMultimap, new Schedule(1, 11, 10), new Position(-114, null), 3, 9);
        put(positionMultimap, new Schedule(1, 12, 10), new Position(-105, null), 3, 9);
        put(positionMultimap, new Schedule(1, 13, 10), new Position(-96, null), 3, 9);
        put(positionMultimap, new Schedule(1, 14, 10), new Position(-105, null), 3, 9);
        put(positionMultimap, new Schedule(1, 15, 10), new Position(-114, null), 3, 9);
        put(positionMultimap, new Schedule(1, 16, 10), new Position(-125, null), 3, 9);
        put(positionMultimap, new Schedule(1, 19, 10), new Position(-135, null), 3, 9);

        put(positionMultimap, new Schedule(1, 8, 10), new Position(-122, null), 4, 8);
        put(positionMultimap, new Schedule(1, 9, 10), new Position(-110, null), 4, 8);
        put(positionMultimap, new Schedule(1, 10, 10), new Position(-98, null), 4, 8);
        put(positionMultimap, new Schedule(1, 11, 10), new Position(-86, null), 4, 8);
        put(positionMultimap, new Schedule(1, 12, 10), new Position(-74, null), 4, 8);
        put(positionMultimap, new Schedule(1, 13, 10), new Position(-62, null), 4, 8);
        put(positionMultimap, new Schedule(1, 14, 10), new Position(-74, null), 4, 8);
        put(positionMultimap, new Schedule(1, 15, 10), new Position(-86, null), 4, 8);
        put(positionMultimap, new Schedule(1, 16, 10), new Position(-98, null), 4, 8);
        put(positionMultimap, new Schedule(1, 17, 10), new Position(-110, null), 4, 8);
        put(positionMultimap, new Schedule(1, 18, 10), new Position(-122, null), 4, 8);
        put(positionMultimap, new Schedule(1, 19, 10), new Position(-135, null), 4, 8);

        put(positionMultimap, new Schedule(1, 7, 10), new Position(-119, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 8, 10), new Position(-104, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 9, 10), new Position(-89, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 10, 10), new Position(-74, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 11, 10), new Position(-59, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 12, 10), new Position(-44, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 13, 10), new Position(-29, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 14, 10), new Position(-44, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 15, 10), new Position(-59, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 16, 10), new Position(-74, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 17, 10), new Position(-89, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 18, 10), new Position(-104, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 19, 10), new Position(-119, null), 5, 6, 7);
        put(positionMultimap, new Schedule(1, 20, 10), new Position(-135, null), 5, 6, 7);

        clocks.add(new Clock(6, 30));
        clocks.add(new Clock(7, 10));
        clocks.add(new Clock(8, 10));
        clocks.add(new Clock(9, 10));
        clocks.add(new Clock(10, 10));
        clocks.add(new Clock(11, 10));
        clocks.add(new Clock(11, 40));
        clocks.add(new Clock(12, 10));
        clocks.add(new Clock(12, 40));
        clocks.add(new Clock(13, 10));
        clocks.add(new Clock(13, 40));
        clocks.add(new Clock(14, 10));
        clocks.add(new Clock(14, 40));
        clocks.add(new Clock(15, 10));
        clocks.add(new Clock(16, 10));
        clocks.add(new Clock(17, 10));
        clocks.add(new Clock(18, 10));
        clocks.add(new Clock(19, 10));
        clocks.add(new Clock(20, 10));
        clocks.add(new Clock(21, 0));
    }

    @Test
    public void generateInserts() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, 0, 1, 0, 0, 0);
        for (int i = 0; i < 366; i++) {
            int month = calendar.get(Calendar.MONTH) + 1;
            int dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
            for (Clock clock : clocks) {
                Collection<Position> positions = positionMultimap.get(new Schedule(month, clock.hour, clock.minute));
                if (positions != null && !positions.isEmpty()) {
                    Integer horizontal = null;
                    Integer vertical = null;
                    for (Position position : positions) {
                        if (horizontal != null && position.horizontal != null || vertical != null && position.vertical != null)
                            throw new RuntimeException();
                        if (position.horizontal != null) horizontal = position.horizontal;
                        if (position.vertical != null) vertical = position.vertical;
                    }
                    int tmpIndex = index++;
                    System.out.println("INSERT INTO SOLAR_POSITION(ID,HORIZONTAL,VERTICAL) VALUES (" + tmpIndex + "," + horizontal + "," + vertical + ");");
                    System.out.println("INSERT INTO SOLAR_SCHEDULE(MONTH,DAY,HOUR,MINUTE,POSITION) VALUES (" + month + "," + dayInMonth + "," + clock.hour + "," + clock.minute + ", " + tmpIndex + ");");
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

    }


    private void put(Multimap<Schedule, Position> positionMultimap, Schedule key, Position value, Integer... months) {
        for (int month : months) {
            Schedule schedule = key.cloneIt();
            schedule.month = month;
            positionMultimap.put(schedule, value.cloneIt());
        }
    }

    private void monthly(Multimap<Schedule, Position> positionMultimap, Schedule key, Position value) {
        for (int i = 1; i < 13; i++) {
            Schedule schedule = key.cloneIt();
            schedule.month = i;
            positionMultimap.put(schedule, value.cloneIt());
        }
    }

    public static class Schedule {
        int month;
        int hour;
        int minute;

        public Schedule(int month, int hour, int minute) {
            this.month = month;
            this.hour = hour;
            this.minute = minute;
        }

        Schedule cloneIt() {
            return new Schedule(month, hour, minute);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Schedule schedule = (Schedule) o;
            return month == schedule.month &&
                    hour == schedule.hour &&
                    minute == schedule.minute;
        }

        @Override
        public int hashCode() {
            return Objects.hash(month, hour, minute);
        }
    }

    public static class Clock {
        int hour;
        int minute;

        public Clock(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }
    }

    public static class Position {
        Integer horizontal;
        Integer vertical;

        public Position(Integer horizontal, Integer vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        Position cloneIt() {
            return new Position(horizontal, vertical);
        }
    }


}
