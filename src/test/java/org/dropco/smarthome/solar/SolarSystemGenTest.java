package org.dropco.smarthome.solar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Ignore
public class SolarSystemGenTest {
    int index = 4;

    private Table<Integer, Integer, Position> base() {
        Table<Integer, Integer, Position> positionTable = HashBasedTable.create();
        positionTable.put(6, 30, new Position(135, 245));
        positionTable.put(11, 40, new Position(null, 215));
        positionTable.put(12, 10, new Position(null, 185));
        positionTable.put(12, 40, new Position(null, 155));
        positionTable.put(13, 10, new Position(null, 125));
        positionTable.put(13, 40, new Position(null, 95));
        positionTable.put(14, 10, new Position(null, 65));
        positionTable.put(14, 40, new Position(null, 35));
        positionTable.put(15, 10, new Position(null, 0));
        return positionTable;
    }

    private Table<Integer, Integer, Position> prepareJanuary() {
        Table<Integer, Integer, Position> base = base();
        merge(base, 17, 10, new Position(0, 0));
        return base;
    }

    private Table<Integer, Integer, Position> prepareFeb() {
        Table<Integer, Integer, Position> base = base();
        merge(base, 10, 10, new Position(125, null));
        merge(base, 11, 10, new Position(116, null));
        merge(base, 12, 10, new Position(107, null));
        merge(base, 13, 10, new Position(98, null));
        merge(base, 14, 10, new Position(107, null));
        merge(base, 15, 10, new Position(116, null));
        merge(base, 16, 10, new Position(125, null));
        merge(base, 17, 10, new Position(0, 0));
        return base;
    }

    private Table<Integer, Integer, Position> prepareMarch() {
        Table<Integer, Integer, Position> base = base();
        merge(base, 8, 10, new Position(125, null));
        merge(base, 9, 10, new Position(116, null));
        merge(base, 10, 10, new Position(107, null));
        merge(base, 11, 10, new Position(98, null));
        merge(base, 12, 10, new Position(89, null));
        merge(base, 13, 10, new Position(80, null));
        merge(base, 14, 10, new Position(89, null));
        merge(base, 15, 10, new Position(98, null));
        merge(base, 16, 10, new Position(107, null));
        merge(base, 17, 10, new Position(116, null));
        merge(base, 18, 10, new Position(125, null));
        merge(base, 21, 00, new Position(0, 0));
        return base;
    }

    private Table<Integer, Integer, Position> prepareApril() {
        Table<Integer, Integer, Position> base = base();
        merge(base, 8, 10, new Position(122, null));
        merge(base, 9, 10, new Position(110, null));
        merge(base, 10, 10, new Position(98, null));
        merge(base, 11, 10, new Position(86, null));
        merge(base, 12, 10, new Position(74, null));
        merge(base, 13, 10, new Position(62, null));
        merge(base, 14, 10, new Position(74, null));
        merge(base, 15, 10, new Position(86, null));
        merge(base, 16, 10, new Position(98, null));
        merge(base, 17, 10, new Position(110, null));
        merge(base, 18, 10, new Position(122, null));
        merge(base, 21, 00, new Position(0, 0));
        return base;
    }

    private Table<Integer, Integer, Position> prepareMay() {
        Table<Integer, Integer, Position> base = base();
        merge(base, 7, 10, new Position(119, null));
        merge(base, 8, 10, new Position(104, null));
        merge(base, 9, 10, new Position(89, null));
        merge(base, 10, 10, new Position(74, null));
        merge(base, 11, 10, new Position(59, null));
        merge(base, 12, 10, new Position(44, null));
        merge(base, 13, 10, new Position(29, null));
        merge(base, 14, 10, new Position(44, null));
        merge(base, 15, 10, new Position(59, null));
        merge(base, 16, 10, new Position(74, null));
        merge(base, 17, 10, new Position(89, null));
        merge(base, 18, 10, new Position(104, null));
        merge(base, 19, 10, new Position(119, null));
        merge(base, 21, 00, new Position(0, 0));
        return base;
    }

    private Table<Integer, Integer, Position> prepareJune() {
        return prepareMay();
    }

    private Table<Integer, Integer, Position> prepareJuly() {
        return prepareMay();
    }

    private Table<Integer, Integer, Position> prepareAug() {
        return prepareApril();
    }

    private Table<Integer, Integer, Position> prepareSep() {
        return prepareMarch();
    }

    private Table<Integer, Integer, Position> prepareOct() {
        Table<Integer, Integer, Position> positionTable = prepareFeb();
        positionTable.remove(17, 10);
        merge(positionTable, 21, 00, new Position(0, 0));
        return positionTable;
    }

    private Table<Integer, Integer, Position> prepareNov() {
        return prepareJanuary();
    }

    private Table<Integer, Integer, Position> prepareDec() {
        Table<Integer, Integer, Position> base = base();
        merge(base, 17, 10, new Position(0, 0));
        return base;
    }

    private void merge(Table<Integer, Integer, Position> base, int hour, int minute, Position position) {
        if (base.contains(hour, minute)) {
            Position prevPosition = base.get(hour, minute);
            if (position.horizontal != null) prevPosition.horizontal = position.horizontal;
            if (position.vertical != null) prevPosition.vertical = position.vertical;
        } else base.put(hour, minute, position);
    }

    @Test
    public void generateInserts() throws IOException {
        PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File("C:\\SRDEV\\Projects\\heating\\changes.sql"))));
        print(prepareJanuary(), 1, 31, out);
        print(prepareFeb(), 2, 29, out);
        print(prepareMarch(), 3, 31, out);
        print(prepareApril(), 4, 30, out);
        print(prepareMay(), 5, 31, out);
        print(prepareJune(), 6, 30, out);
        print(prepareJuly(), 7, 31, out);
        print(prepareAug(), 8, 31, out);
        print(prepareSep(), 9, 30, out);
        print(prepareOct(), 10, 31, out);
        print(prepareNov(), 11, 30, out);
        print(prepareDec(), 12, 31, out);
        out.flush();
        out.close();
    }


    private void print(Table<Integer, Integer, Position> positions, int month, int maxDays, PrintWriter out) {
        List<Table.Cell<Integer, Integer, Position>> sorted = Lists.newArrayList(positions.cellSet());
        Collections.sort(sorted, (o1, o2) -> {
            int res = Integer.compare(o1.getRowKey(), o2.getRowKey());
            if (res == 0) return Integer.compare(o1.getColumnKey(), o2.getColumnKey());
            return res;
        });
        Integer hor = null;
        Integer ver = null;
        for (Table.Cell<Integer, Integer, Position> entry : sorted) {
            if (entry.getValue().vertical!=null) ver = entry.getValue().vertical;
            if (entry.getValue().horizontal!=null) hor = entry.getValue().horizontal;
            entry.getValue().horizontal = Optional.ofNullable(entry.getValue().horizontal).orElse(hor);
            entry.getValue().vertical = Optional.ofNullable(entry.getValue().vertical).orElse(ver);
        }
        for (int dayInMonth = 1; dayInMonth <= maxDays; dayInMonth++) {
            int finalDayInMonth = dayInMonth;
            sorted.forEach(cell -> {
                        int tmpIndex = index++;
                        int hour = cell.getRowKey();
                        int minute = cell.getColumnKey();
                        Integer horizontal = cell.getValue().horizontal == null ? null : (int) Math.round(cell.getValue().horizontal * 2.816326530612245);
                        Integer vertical = cell.getValue().vertical == null ? null : (int) Math.round(cell.getValue().vertical * 3.185185185185185);
                        out.println("INSERT INTO `SOLAR_POSITION`(ID,HORIZONTAL,VERTICAL) VALUES (" + tmpIndex + "," + horizontal + "," + vertical + ");");
                        out.println("INSERT INTO `SOLAR_SCHEDULE`(MONTH,DAY,HOUR,MINUTE,POSITION) VALUES (" + month + "," + finalDayInMonth + "," + hour + "," + minute + ", " + tmpIndex + ");");
                    }
            );

        }
    }


    public static class Position {
        Integer horizontal;
        Integer vertical;

        public Position(Integer vertical, Integer horizontal) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        Position cloneIt() {
            return new Position(horizontal, vertical);
        }
    }


}
