package org.dropco.smarthome.watering.db;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.querydsl.core.Tuple;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.DBConnection;
import org.dropco.smarthome.database.querydsl.WateringZone;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import static org.dropco.smarthome.database.querydsl.WateringZone.WATERING_ZONE;

public class WateringDao {

    public List<WateringRecord> getAllRecords() {
        List<Tuple> lst = new MySQLQuery<WateringZone>(getConnection()).select(WATERING_ZONE.all())
                .from(WATERING_ZONE).fetch();
        return FluentIterable.from(lst).transform(tuple -> toRecord(tuple)).toList();
    }

    public List<WateringRecord> getActiveRecords() {
        List<Tuple> lst = new MySQLQuery<WateringZone>(getConnection()).select(WATERING_ZONE.all())
                .from(WATERING_ZONE).where(WATERING_ZONE.active.eq(true)).
                fetch();
        return FluentIterable.from(lst).transform(tuple -> toRecord(tuple)).toList();
    }

    public Set<String> getActiveZones() {
        return Sets.newHashSet(new MySQLQuery<WateringZone>(getConnection()).
                select(WATERING_ZONE.pinZoneRefCd).from(WATERING_ZONE).distinct().fetch());
    }
    private WateringRecord toRecord(Tuple tuple) {
        WateringRecord record = new WateringRecord();
        record.setHour(tuple.get(WATERING_ZONE.hour));
        record.setRetryHour(tuple.get(WATERING_ZONE.retryHour));
        record.setMinute(tuple.get(WATERING_ZONE.minute));
        record.setRetryMinute(tuple.get(WATERING_ZONE.retryMinute));
        record.setModulo(tuple.get(WATERING_ZONE.modulo));
        record.setReminder(tuple.get(WATERING_ZONE.reminder));
        record.setTimeInSeconds(tuple.get(WATERING_ZONE.timeInSeconds));
        record.setZoneRefCode(tuple.get(WATERING_ZONE.pinZoneRefCd));
        record.setActive(tuple.get(WATERING_ZONE.active));
        return record;
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }


}
