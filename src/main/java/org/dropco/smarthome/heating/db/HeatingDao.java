package org.dropco.smarthome.heating.db;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.DBConnection;
import org.dropco.smarthome.database.querydsl.SolarHeating;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.heating.dto.SolarHeatingSchedule;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.dropco.smarthome.database.querydsl.SolarHeating.*;
import static org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace.TEMP_MEASURE_PLACE;

public class HeatingDao {

    public String getDeviceId(String placeRefCd) {
        return new MySQLQuery<StringSetting>(getConnection()).select(TEMP_MEASURE_PLACE.devideId)
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.placeRefCd.eq(placeRefCd)).fetchFirst();
    }

    public String getPlaceRefCd(String deviceId) {
        return new MySQLQuery<StringSetting>(getConnection()).select(TEMP_MEASURE_PLACE.placeRefCd)
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.devideId.eq(deviceId)).fetchFirst();
    }

    public SolarHeatingSchedule getCurrentRecord(){
        Tuple tuple = new MySQLQuery<StringSetting>(getConnection()).select(SOLAR_HEATING.all())
                .from(SOLAR_HEATING).where(SOLAR_HEATING.day.eq(LocalDate.now().getDayOfWeek().getValue())
                        .and(SOLAR_HEATING.fromTime.loe(LocalTime.now()).and(SOLAR_HEATING.toTime.gt(LocalTime.now())))).fetchFirst();
        return toSolarHeating(tuple);

    }

    public Tuple getMeasurePlaceByRefCd(String refCd) {
        return new MySQLQuery<StringSetting>(getConnection()).select(TEMP_MEASURE_PLACE.all())
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.placeRefCd.eq(refCd)).fetchFirst();
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }


    public List<Tuple> listMeasurePlaces() {
        return new MySQLQuery<StringSetting>(getConnection()).select(TEMP_MEASURE_PLACE.all())
                .from(TEMP_MEASURE_PLACE).fetch();
    }

    public void saveMeasurePlace(String name,String refCd, String deviceId) {
        new SQLInsertClause(getConnection(), SQLTemplates.DEFAULT, TEMP_MEASURE_PLACE)
                .set(TEMP_MEASURE_PLACE.devideId, deviceId)
                .set(TEMP_MEASURE_PLACE.name, name)
                .set(TEMP_MEASURE_PLACE.placeRefCd, refCd)
                .execute();
    }

    public void updateMeasurePlace(String name, String deviceId, String refCd) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, TEMP_MEASURE_PLACE)
                .set(TEMP_MEASURE_PLACE.devideId, deviceId)
                .set(TEMP_MEASURE_PLACE.name, name)
                .where(TEMP_MEASURE_PLACE.placeRefCd.eq(refCd))
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
                .setToTime(tuple.get(SOLAR_HEATING.toTime))
                .setThreeWayValveStopDiff(tuple.get(SOLAR_HEATING.threeWayValveStopDiff))
                .setThreeWayValveStartDiff(tuple.get(SOLAR_HEATING.threeWayValveStartDiff))
                .setBoilerBlock(tuple.get(SOLAR_HEATING.boilerBlocked));
    }
}
