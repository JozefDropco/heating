package org.dropco.smarthome.database;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.database.querydsl.TemperatureLog;

import java.util.Date;
import java.util.List;

import static org.dropco.smarthome.database.DBConnection.getConnection;
import static org.dropco.smarthome.database.querydsl.LongSetting.LONG;
import static org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace.TEMP_MEASURE_PLACE;

public class LogDao {

    private static final Template leaveoutminutes = TemplateFactory.DEFAULT.create("STR_TO_DATE(DATE_FORMAT({0},'%Y-%m-%d %H'),'%Y-%m-%d %H')");
   public static TemperatureLog _log = new TemperatureLog("log");

    public void logTemperature(String deviceId, String placeRefCd, Date date, double temperature) {
        new SQLInsertClause(getConnection(), SQLTemplates.DEFAULT, _log)
                .set(_log.devideId, deviceId)
                .set(_log.placeRefCd, placeRefCd)
                .set(_log.timestamp, date)
                .set(_log.value, temperature)
                .execute();
    }

    public List<Tuple> retrieveTemperaturesWithPlaces(Date from, Date to) {
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class,leaveoutminutes, _log.timestamp);
        return new MySQLQuery<StringSetting>(getConnection()).select(_log.placeRefCd,_log.devideId,
                removeMinutes.as(_log.timestamp),
               _log.value.avg().as(_log.value)).from(_log).
               where(_log.placeRefCd.isNotNull()
                       .and(_log.timestamp.goe(from))
               .and(_log.timestamp.loe(to))
               )
               .groupBy(_log.placeRefCd,removeMinutes,_log.devideId)
               .orderBy(_log.placeRefCd.asc(),_log.timestamp.asc(), _log.devideId.asc()).fetch();


    }

    public List<String> listUnassignedDeviceIds() {
        return new MySQLQuery<StringSetting>(getConnection()).select(_log.devideId).from(_log).
                where(_log.placeRefCd.isNull()).fetch();
    }

    public void updateLogs(String deviceId, String refCd) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, _log)
                .set(_log.placeRefCd, refCd)
                .where(_log.devideId.eq(deviceId))
                .execute();
    }
}
