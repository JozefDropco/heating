package org.dropco.smarthome.database;

import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.StringSetting;

import java.sql.Connection;

import static org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace.TEMP_MEASURE_PLACE;

public class HeatingDao {

    public String getDeviceId(String solarPanelTemperaturePlaceRefCd) {
        return new MySQLQuery<StringSetting>(getConnection()).select(TEMP_MEASURE_PLACE.devideId)
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.placeRefCd.eq(solarPanelTemperaturePlaceRefCd)).fetchFirst();
    }

    public String getPlaceRefCd(String deviceId) {
        return new MySQLQuery<StringSetting>(getConnection()).select(TEMP_MEASURE_PLACE.placeRefCd)
                .from(TEMP_MEASURE_PLACE).where(TEMP_MEASURE_PLACE.devideId.eq(deviceId)).fetchFirst();
    }
    private Connection getConnection() {
        return DBConnection.getConnection();
    }
}
