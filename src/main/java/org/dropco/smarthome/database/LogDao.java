package org.dropco.smarthome.database;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.database.querydsl.TemperatureLog;

import java.util.Date;
import java.util.List;

import static org.dropco.smarthome.database.DBConnection.getConnection;

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
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class, leaveoutminutes, _log.timestamp);
        return new MySQLQuery<StringSetting>(getConnection()).select(_log.placeRefCd,
                removeMinutes.as(_log.timestamp),
                _log.value.avg().as(_log.value)).from(_log).
                where(_log.placeRefCd.isNotNull()
                        .and(_log.timestamp.goe(from))
                        .and(_log.timestamp.loe(to))
                )
                .groupBy(_log.placeRefCd, removeMinutes)
                .orderBy(_log.placeRefCd.asc(), _log.timestamp.asc()).fetch();


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

    public List<AggregateTemp> retrieveAggregatedTemperatures(Date from, Date to) {
        NumberExpression<Double> min = _log.value.min().as("min");
        NumberExpression<Double> max = _log.value.max().as("max");
        NumberExpression<Double> avg = _log.value.avg().as("avg");
        List<Tuple> result = new MySQLQuery<StringSetting>(getConnection()).select(_log.placeRefCd,
                min,
                max,
                avg
        ).from(_log).
                where(_log.placeRefCd.isNotNull()
                        .and(_log.timestamp.goe(from))
                        .and(_log.timestamp.loe(to))
                )
                .groupBy(_log.placeRefCd)
                .orderBy(_log.placeRefCd.asc()).fetch();
        return Lists.transform(result, tuple -> {
            AggregateTemp temp = new AggregateTemp();
            temp.measurePlace = tuple.get(_log.placeRefCd);
            temp.min = tuple.get(min);
            temp.max = tuple.get(max);
            temp.avg = tuple.get(avg);
            return temp;
        });

    }

    public static class AggregateTemp {
        public String measurePlace;
        public double min;
        public double max;
        public double avg;

    }
}
