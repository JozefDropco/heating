package org.dropco.smarthome.watering.db;

import com.google.common.collect.FluentIterable;
import com.querydsl.core.Tuple;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.querydsl.WateringZone;
import org.dropco.smarthome.dto.NamedPort;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import static org.dropco.smarthome.database.querydsl.WateringZone.WATERING_ZONE;

public class WateringDao implements Dao {

    protected static final SQLTemplates DEFAULT = new MySQLTemplates();
    private Connection connection;

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

    public WateringRecord getRecord(Long id) {
        return toRecord(new MySQLQuery<WateringZone>(getConnection()).select(WATERING_ZONE.all())
                .from(WATERING_ZONE).where(WATERING_ZONE.id.eq(id)).
                fetchFirst());
    }

    public Set<NamedPort> getActiveZones() {
        List<Tuple> tuples = new MySQLQuery<WateringZone>(getConnection()).
                select(WATERING_ZONE.pinZoneRefCd, WATERING_ZONE.name).from(WATERING_ZONE).distinct().fetch();
        return FluentIterable.from(tuples).transform(tuple -> new NamedPort(tuple.get(WATERING_ZONE.pinZoneRefCd), tuple.get(WATERING_ZONE.name))).toSet();
    }

    private WateringRecord toRecord(Tuple tuple) {
        WateringRecord record = new WateringRecord();
        record.setId(tuple.get(WATERING_ZONE.id));
        record.setHour(tuple.get(WATERING_ZONE.hour));
        record.setName(tuple.get(WATERING_ZONE.name));
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
        return connection;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void updateRecord(WateringRecord record) {
        SQLUpdateClause clause = new SQLUpdateClause(getConnection(), DEFAULT, WATERING_ZONE);
        clause.set(WATERING_ZONE.reminder, record.getReminder());
        clause.set(WATERING_ZONE.modulo, record.getModulo());
        clause.set(WATERING_ZONE.retryMinute, record.getRetryMinute());
        clause.set(WATERING_ZONE.retryHour, record.getRetryHour());
        clause.set(WATERING_ZONE.active, record.isActive());
        clause.set(WATERING_ZONE.hour, record.getHour());
        clause.set(WATERING_ZONE.minute, record.getMinute());
        clause.set(WATERING_ZONE.pinZoneRefCd, record.getZoneRefCode());
        clause.set(WATERING_ZONE.name, record.getName());
        clause.set(WATERING_ZONE.timeInSeconds, record.getTimeInSeconds());
        clause.where(WATERING_ZONE.id.eq(record.getId()));
        clause.execute();
    }

    public long insertRecord(WateringRecord record) {
        SQLInsertClause clause = new SQLInsertClause(getConnection(), DEFAULT, WATERING_ZONE);
        clause.set(WATERING_ZONE.reminder, record.getReminder());
        clause.set(WATERING_ZONE.modulo, record.getModulo());
        clause.set(WATERING_ZONE.retryMinute, record.getRetryMinute());
        clause.set(WATERING_ZONE.retryHour, record.getRetryHour());
        clause.set(WATERING_ZONE.active, record.isActive());
        clause.set(WATERING_ZONE.hour, record.getHour());
        clause.set(WATERING_ZONE.minute, record.getMinute());
        clause.set(WATERING_ZONE.pinZoneRefCd, record.getZoneRefCode());
        clause.set(WATERING_ZONE.name, record.getName());
        clause.set(WATERING_ZONE.timeInSeconds, record.getTimeInSeconds());
        return clause.executeWithKey(WATERING_ZONE.id);
    }

}
