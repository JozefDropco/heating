package org.dropco.smarthome.heating.solar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;

@Ignore
public class SolarSystemGenTest {
    private SolarSchedule base() {
        Table<Integer, Integer, Move> MoveTable = HashBasedTable.create();
        SolarSchedule s = new SolarSchedule();
        s.sunRiseHour = 6;
        s.sunRiseMinute = 30;
        MoveTable.put(11, 40, new Move(0, -1));
        MoveTable.put(12, 10, new Move(0, -1));
        MoveTable.put(12, 40, new Move(0, -1));
        MoveTable.put(13, 10, new Move(0, -1));
        MoveTable.put(13, 40, new Move(0, -1));
        MoveTable.put(14, 10, new Move(0, -1));
        MoveTable.put(14, 40, new Move(0, -1));
        MoveTable.put(15, 10, new Move(0, -1));
        s.dailyMoves = MoveTable;
        s.horizontalStep = 30.625;
        s.verticalStep = 9;

        return s;
    }

    private SolarSchedule prepareJanuary() {
        SolarSchedule base = base();
        base.month = 1;
        base.sunSetHour = 17;
        base.sunSetMinute = 10;
        return base;
    }

    private SolarSchedule prepareFeb() {
        SolarSchedule base = base();
        base.month = 2;
        base.sunSetHour = 17;
        base.sunSetMinute = 10;
        base.verticalStep = 9;
        merge(base, 10, 10, new Move(-1, 0));
        merge(base, 11, 10, new Move(-1, 0));
        merge(base, 12, 10, new Move(-1, 0));
        merge(base, 13, 10, new Move(-1, 0));
        merge(base, 14, 10, new Move(1, 0));
        merge(base, 15, 10, new Move(1, 0));
        merge(base, 16, 10, new Move(1, 0));
        return base;
    }

    private SolarSchedule prepareMarch() {
        SolarSchedule base = base();
        base.month = 3;
        base.sunSetHour = 21;
        base.sunSetMinute = 0;
        base.verticalStep = 9;

        merge(base, 8, 10, new Move(-1, 0));
        merge(base, 9, 10, new Move(-1, 0));
        merge(base, 10, 10, new Move(-1, 0));
        merge(base, 11, 10, new Move(-1, 0));
        merge(base, 12, 10, new Move(-1, 0));
        merge(base, 13, 10, new Move(-1, 0));
        merge(base, 14, 10, new Move(1, 0));
        merge(base, 15, 10, new Move(1, 0));
        merge(base, 16, 10, new Move(1, 0));
        merge(base, 17, 10, new Move(1, 0));
        merge(base, 18, 10, new Move(1, 0));
        return base;
    }

    private SolarSchedule prepareApril() {
        SolarSchedule base = base();
        base.month = 4;
        base.sunSetHour = 21;
        base.sunSetMinute = 0;
        base.verticalStep = 12;
        merge(base, 8, 10, new Move(-1, 0));
        merge(base, 9, 10, new Move(-1, 0));
        merge(base, 10, 10, new Move(-1, 0));
        merge(base, 11, 10, new Move(-1, 0));
        merge(base, 12, 10, new Move(-1, 0));
        merge(base, 13, 10, new Move(-1, 0));
        merge(base, 14, 10, new Move(1, 0));
        merge(base, 15, 10, new Move(1, 0));
        merge(base, 16, 10, new Move(1, 0));
        merge(base, 17, 10, new Move(1, 0));
        merge(base, 18, 10, new Move(1, 0));
        return base;
    }

    private SolarSchedule prepareMay() {
        SolarSchedule base = base();
        base.month = 5;
        base.sunSetHour = 21;
        base.sunSetMinute = 0;
        base.verticalStep = 15;
        merge(base, 7, 10, new Move(-1, 0));
        merge(base, 8, 10, new Move(-1, 0));
        merge(base, 9, 10, new Move(-1, 0));
        merge(base, 10, 10, new Move(-1, 0));
        merge(base, 11, 10, new Move(-1, 0));
        merge(base, 12, 10, new Move(-1, 0));
        merge(base, 13, 10, new Move(-1, 0));
        merge(base, 14, 10, new Move(1, 0));
        merge(base, 15, 10, new Move(1, 0));
        merge(base, 16, 10, new Move(1, 0));
        merge(base, 17, 10, new Move(1, 0));
        merge(base, 18, 10, new Move(1, 0));
        merge(base, 19, 10, new Move(1, 0));
        return base;
    }

    private SolarSchedule prepareJune() {
        SolarSchedule solarSchedule = prepareMay();
        solarSchedule.month = 6;
        return solarSchedule;
    }

    private SolarSchedule prepareJuly() {
        SolarSchedule solarSchedule = prepareMay();
        solarSchedule.month = 7;
        return solarSchedule;
    }

    private SolarSchedule prepareAug() {
        SolarSchedule solarSchedule = prepareApril();
        solarSchedule.month = 8;
        return solarSchedule;
    }

    private SolarSchedule prepareSep() {
        SolarSchedule solarSchedule = prepareMarch();
        solarSchedule.month = 9;
        return solarSchedule;
    }

    private SolarSchedule prepareOct() {
        SolarSchedule base = prepareFeb();
        base.month = 10;
        base.sunSetHour = 21;
        base.sunSetMinute = 0;
        return base;
    }

    private SolarSchedule prepareNov() {
        SolarSchedule solarSchedule = prepareJanuary();
        solarSchedule.month = 11;
        return solarSchedule;
    }

    private SolarSchedule prepareDec() {
        SolarSchedule solarSchedule = prepareJanuary();
        solarSchedule.month = 12;
        return solarSchedule;
    }

    private void merge(SolarSchedule base, int hour, int minute, Move move) {
        if (base.dailyMoves.contains(hour, minute)) {
            Move prevMove = base.dailyMoves.get(hour, minute);
            prevMove.horMove += move.horMove;
            prevMove.verMove += move.verMove;
        } else base.dailyMoves.put(hour, minute, move);
    }

    @Test
    public void generateInserts() throws IOException {
        PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File("C:\\SRDEV\\Projects\\heating\\changes.sql"))));
        out.println("DROP TABLE `SOLAR_SCHEDULE`;");
        out.println("DROP TABLE `SOLAR_POSITION`;");
        out.println("create table `SOLAR_SCHEDULE`\n" +
                "(\n" +
                "\tMONTH int not null,\n" +
                "\tHORIZONTAL_STEP int not null,\n" +
                "\tVERTICAL_STEP int not null,\n" +
                "\tSUN_RISE_HOUR int not null,\n" +
                "\tSUN_RISE_MINUTE int not null,\n" +
                "\tSUN_RISE_ABS_POS_HOR int not null,\n" +
                "\tSUN_RISE_ABS_POS_VERT int not null,\n" +
                "\tSUN_SET_HOUR int not null,\n" +
                "\tSUN_SET_MINUTE int not null,\n" +
                "\tSUN_SET_ABS_POS_HOR int not null,\n" +
                "\tSUN_SET_ABS_POS_VERT int not null,\n" +
                "\tconstraint SOLAR_SCHEDULE_pk\n" +
                "\t\tprimary key (MONTH)\n" +
                ");\n" +
                "\n");
        out.println("create table `SOLAR_MOVE`\n" +
                "(\n" +
                "\tMONTH int not null,\n" +
                "\tHOUR int not null,\n" +
                "\tMINUTE int not null,\n" +
                "\tHORIZONTAL int not null,\n" +
                "\tVERTICAL int not null,\n" +
                "\tconstraint SOLAR_MOVE_pk\n" +
                "\t\tprimary key (MONTH, HOUR, MINUTE)\n" +
                ");\n" +
                "\n");
        print(prepareJanuary(), out);
        print(prepareFeb(), out);
        print(prepareMarch(), out);
        print(prepareApril(), out);
        print(prepareMay(), out);
        print(prepareJune(), out);
        print(prepareJuly(), out);
        print(prepareAug(), out);
        print(prepareSep(), out);
        print(prepareOct(), out);
        print(prepareNov(), out);
        print(prepareDec(), out);
        out.flush();
        out.close();
    }


    private void print(SolarSchedule schedule, PrintWriter out) {
        List<Table.Cell<Integer, Integer, Move>> sorted = Lists.newArrayList(schedule.dailyMoves.cellSet());
        Collections.sort(sorted, (o1, o2) -> {
            int res = Integer.compare(o1.getRowKey(), o2.getRowKey());
            if (res == 0) return Integer.compare(o1.getColumnKey(), o2.getColumnKey());
            return res;
        });
        schedule.verticalStep = Math.round(schedule.verticalStep * 3.185185185185185);
        schedule.horizontalStep = Math.round(schedule.horizontalStep * 2.816326530612245);
        out.println(String.format("INSERT INTO `SOLAR_SCHEDULE`(MONTH,HORIZONTAL_STEP,VERTICAL_STEP," +
                        "SUN_RISE_HOUR, SUN_RISE_MINUTE, SUN_RISE_ABS_POS_HOR,SUN_RISE_ABS_POS_VERT, " +
                        "SUN_SET_HOUR, SUN_SET_MINUTE, SUN_SET_ABS_POS_HOR,SUN_SET_ABS_POS_VERT) VALUES (%s,%s,%s," +
                        "%s,%s,%s,%s," +
                        "%s,%s,%s,%s);",
                schedule.month, (int)schedule.horizontalStep, (int)schedule.verticalStep,
                schedule.sunRiseHour, schedule.sunRiseMinute, 690, 430,
                schedule.sunSetHour, schedule.sunSetMinute, 0, 0));

        sorted.forEach(cell -> {
                    int hour = cell.getRowKey();
                    int minute = cell.getColumnKey();
                    out.println(String.format("INSERT INTO `SOLAR_MOVE`(MONTH, HOUR,MINUTE,HORIZONTAL,VERTICAL) VALUES (%s,%s,%s,%s,%s);", schedule.month, hour, minute, cell.getValue().horMove, cell.getValue().verMove));
                }
        );

    }


    public static class SolarSchedule {
        int month;
        double horizontalStep;
        double verticalStep;
        int sunRiseHour;
        int sunRiseMinute;
        int sunSetHour;
        int sunSetMinute;
        Table<Integer, Integer, Move> dailyMoves = HashBasedTable.create();

        SolarSchedule cloneIt() {
            SolarSchedule s = new SolarSchedule();
            s.month = month;
            s.horizontalStep = horizontalStep;
            s.verticalStep = verticalStep;
            s.dailyMoves = HashBasedTable.create(dailyMoves);
            return s;
        }
    }

    private static class Move {
        int horMove;
        int verMove;

        public Move(int verMove, int horMove) {
            this.horMove = horMove;
            this.verMove = verMove;
        }
    }

}
