package org.dropco.smarthome.database;

import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLInsertClause;
import org.dropco.smarthome.database.querydsl.TemperatureLog;

import java.util.Date;

import static org.dropco.smarthome.database.DBConnection.getConnection;

public class LogDao {
    TemperatureLog _log = new TemperatureLog("log");

    public void logTemperature(String deviceId, String placeRefCd, Date date, double temperature) {
        new SQLInsertClause(getConnection(), SQLTemplates.DEFAULT, _log)
                .set(_log.devideId, deviceId)
                .set(_log.placeRefCd, placeRefCd)
                .set(_log.timestamp, date)
                .set(_log.value, temperature)
                .execute();
    }
}
