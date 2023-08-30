package org.dropco.smarthome.heating.db;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace;
import org.dropco.smarthome.heating.dto.SolarHeatingSchedule;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dropco.smarthome.database.querydsl.SolarHeating.SOLAR_HEATING;
import static org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace.TEMP_MEASURE_PLACE;

public class HeatingDao implements Dao {

    public static final MySQLTemplates SQL_TEMPLATES = new MySQLTemplates();
    private Connection connection;

    public MeasurePlace getPlaceRefCd(String placeRefCd) {
        return toMeasurePlace(new MySQLQuery<TemperatureMeasurePlace>(getConnection()).select(TEMP_MEASURE_PLACE.all())
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.placeRefCd.eq(placeRefCd)).fetchFirst()).get();
    }

    public Optional<MeasurePlace> getById(String deviceId) {
        return toMeasurePlace(new MySQLQuery<TemperatureMeasurePlace>(getConnection()).select(TEMP_MEASURE_PLACE.all())
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.devideId.eq(deviceId)).fetchFirst());
    }

    public SolarHeatingSchedule getCurrentRecord() {
        Tuple tuple = new MySQLQuery<TemperatureMeasurePlace>(getConnection(), SQL_TEMPLATES).select(SOLAR_HEATING.all())
                .from(SOLAR_HEATING).where(SOLAR_HEATING.day.eq(LocalDate.now().getDayOfWeek().getValue())
                        .and(SOLAR_HEATING.fromTime.loe(LocalTime.now()))).orderBy(SOLAR_HEATING.fromTime.desc()).fetchFirst();
        return toSolarHeating(tuple);

    }
    public SolarHeatingSchedule getNextRecord() {
        Tuple tuple = new MySQLQuery<TemperatureMeasurePlace>(getConnection(), SQL_TEMPLATES).select(SOLAR_HEATING.all())
                .from(SOLAR_HEATING).where(SOLAR_HEATING.day.eq(LocalDate.now().getDayOfWeek().getValue())
                        .and(SOLAR_HEATING.fromTime.gt(LocalTime.now()))).orderBy(SOLAR_HEATING.fromTime.asc()).fetchFirst();
        if (tuple==null) {
            LocalDate now = LocalDate.now();
            now=now.plusDays(1);
            tuple = new MySQLQuery<TemperatureMeasurePlace>(getConnection(), SQL_TEMPLATES).select(SOLAR_HEATING.all())
                    .from(SOLAR_HEATING).where(SOLAR_HEATING.day.eq(now.getDayOfWeek().getValue())).orderBy(SOLAR_HEATING.fromTime.asc()).fetchFirst();
        }
        return toSolarHeating(tuple);

    }


    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public List<MeasurePlace> list() {
        return new MySQLQuery<TemperatureMeasurePlace>(getConnection()).select(TEMP_MEASURE_PLACE.all())
                .from(TEMP_MEASURE_PLACE).fetch().stream().map(HeatingDao::toMeasurePlace).map(Optional::get).collect(Collectors.toList());
    }

    public void saveMeasurePlace(MeasurePlace place) {
        new SQLInsertClause(getConnection(), SQLTemplates.DEFAULT, TEMP_MEASURE_PLACE)
                .set(TEMP_MEASURE_PLACE.devideId, place.getDeviceId())
                .set(TEMP_MEASURE_PLACE.name, place.getName())
                .set(TEMP_MEASURE_PLACE.placeRefCd, place.getRefCd())
                .set(TEMP_MEASURE_PLACE.adjustmentTemp, place.getAdjustmentTemp())
                .set(TEMP_MEASURE_PLACE.orderId, place.getOrderId())
                .execute();
    }

    public void updateMeasurePlace(MeasurePlace place) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, TEMP_MEASURE_PLACE)
                .set(TEMP_MEASURE_PLACE.devideId, place.getDeviceId())
                .set(TEMP_MEASURE_PLACE.name, place.getName())
                .set(TEMP_MEASURE_PLACE.adjustmentTemp, place.getAdjustmentTemp())
                .set(TEMP_MEASURE_PLACE.orderId, place.getOrderId())
                .where(TEMP_MEASURE_PLACE.placeRefCd.eq(place.getRefCd()))
                .execute();
    }

    public void deleteMeasurePlace(String refCd) {
        new SQLDeleteClause(getConnection(), SQLTemplates.DEFAULT, TEMP_MEASURE_PLACE)
                .where(TEMP_MEASURE_PLACE.placeRefCd.eq(refCd))
                .execute();
    }

    private SolarHeatingSchedule toSolarHeating(Tuple tuple) {
        return new SolarHeatingSchedule()
                .setDay(tuple.get(SOLAR_HEATING.day)).setId(tuple.get(SOLAR_HEATING.id))
                .setFromTime(tuple.get(SOLAR_HEATING.fromTime))
                .setThreeWayValveStopDiff(tuple.get(SOLAR_HEATING.threeWayValveStopDiff))
                .setThreeWayValveStartDiff(tuple.get(SOLAR_HEATING.threeWayValveStartDiff))
                .setBoilerBlock(tuple.get(SOLAR_HEATING.boilerBlocked));
    }

    public List<SolarHeatingSchedule> getScheduleForDay(int day) {
        List<Tuple> tuple = new MySQLQuery<StringSetting>(getConnection()).select(SOLAR_HEATING.all())
                .from(SOLAR_HEATING).where(SOLAR_HEATING.day.eq(day)).orderBy(SOLAR_HEATING.fromTime.asc()).fetch();
        return Lists.transform(tuple, this::toSolarHeating);
    }

    public void saveHeatingSchedule(SolarHeatingSchedule solarHeatingSchedule) {
        new SQLInsertClause(getConnection(), SQL_TEMPLATES, SOLAR_HEATING)
                .set(SOLAR_HEATING.fromTime, solarHeatingSchedule.getFromTime())
                .set(SOLAR_HEATING.day, solarHeatingSchedule.getDay())
                .set(SOLAR_HEATING.threeWayValveStartDiff, solarHeatingSchedule.getThreeWayValveStartDiff())
                .set(SOLAR_HEATING.threeWayValveStopDiff, solarHeatingSchedule.getThreeWayValveStopDiff())
                .set(SOLAR_HEATING.boilerBlocked, solarHeatingSchedule.getBoilerBlock())
                .execute();
    }

    public void updateHeatingSchedule(SolarHeatingSchedule solarHeatingSchedule) {
        new SQLUpdateClause(getConnection(), SQL_TEMPLATES, SOLAR_HEATING)
                .set(SOLAR_HEATING.fromTime, solarHeatingSchedule.getFromTime())
                .set(SOLAR_HEATING.day, solarHeatingSchedule.getDay())
                .set(SOLAR_HEATING.threeWayValveStartDiff, solarHeatingSchedule.getThreeWayValveStartDiff())
                .set(SOLAR_HEATING.threeWayValveStopDiff, solarHeatingSchedule.getThreeWayValveStopDiff())
                .set(SOLAR_HEATING.boilerBlocked, solarHeatingSchedule.getBoilerBlock())
                .where(SOLAR_HEATING.id.eq(solarHeatingSchedule.getId()))
                .execute();
    }

    private static Optional<MeasurePlace> toMeasurePlace(Tuple tuple){
        return Optional.ofNullable(tuple).map(t -> new MeasurePlace()
                .setDeviceId(t.get(TEMP_MEASURE_PLACE.devideId))
                .setName(t.get(TEMP_MEASURE_PLACE.name))
                .setAdjustmentTemp(t.get(TEMP_MEASURE_PLACE.adjustmentTemp))
                .setOrderId(t.get(TEMP_MEASURE_PLACE.orderId))
                .setRefCd(t.get(TEMP_MEASURE_PLACE.placeRefCd)));
    }

}
