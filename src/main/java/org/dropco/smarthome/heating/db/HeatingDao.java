package org.dropco.smarthome.heating.db;

import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.DBConnection;
import org.dropco.smarthome.database.querydsl.StringSetting;

import java.sql.Connection;
import java.util.Date;

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

    private Connection getConnection() {
        return DBConnection.getConnection();
    }


}
