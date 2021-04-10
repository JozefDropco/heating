package org.dropco.smarthome.database;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.AppLog;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.database.querydsl.TemperatureLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.dropco.smarthome.database.DBConnection.getConnection;

public class LogDao {

    private static final Template leaveoutminutes = TemplateFactory.DEFAULT.create("STR_TO_DATE(DATE_FORMAT({0},'%Y-%m-%d %H'),'%Y-%m-%d %H')");
    protected static final SQLTemplates SQL_TEMPLATES = new MySQLTemplates();
    public static TemperatureLog _tlog = new TemperatureLog("log");
    public static AppLog _alog = new AppLog("alg");

    public void logTemperature(String deviceId, String placeRefCd, Date date, double temperature) {
        new SQLInsertClause(getConnection(), SQL_TEMPLATES, _tlog)
                .set(_tlog.devideId, deviceId)
                .set(_tlog.placeRefCd, placeRefCd)
                .set(_tlog.timestamp, date)
                .set(_tlog.value, temperature)
                .execute();
    }

    public List<Tuple> retrieveTemperaturesWithPlaces(Date from, Date to) {
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class, leaveoutminutes, _tlog.timestamp);
        return new MySQLQuery<StringSetting>(getConnection()).select(_tlog.placeRefCd,
                removeMinutes.as(_tlog.timestamp),
                _tlog.value.avg().as(_tlog.value)).from(_tlog).
                where(_tlog.placeRefCd.isNotNull()
                        .and(_tlog.timestamp.goe(from))
                        .and(_tlog.timestamp.loe(to))
                )
                .groupBy(removeMinutes, _tlog.placeRefCd)
                .orderBy(_tlog.timestamp.asc(), _tlog.placeRefCd.asc()).fetch();


    }

    public List<String> listUnassignedDeviceIds() {
        return new MySQLQuery<StringSetting>(getConnection()).select(_tlog.devideId).from(_tlog).
                where(_tlog.placeRefCd.isNull()).fetch();
    }

    public void updateLogs(String deviceId, String refCd) {
        new SQLUpdateClause(getConnection(), SQL_TEMPLATES, _tlog)
                .set(_tlog.placeRefCd, refCd)
                .where(_tlog.devideId.eq(deviceId))
                .execute();
    }

    public List<AggregateTemp> retrieveAggregatedTemperatures(Date from, Date to) {
        NumberExpression<Double> min = _tlog.value.min().as("min");
        NumberExpression<Double> max = _tlog.value.max().as("max");
        NumberExpression<Double> avg = _tlog.value.avg().as("avg");
        List<Tuple> result = new MySQLQuery<StringSetting>(getConnection()).select(_tlog.placeRefCd,
                min,
                max,
                avg
        ).from(_tlog).
                where(_tlog.placeRefCd.isNotNull()
                        .and(_tlog.timestamp.goe(from))
                        .and(_tlog.timestamp.loe(to))
                )
                .groupBy(_tlog.placeRefCd)
                .orderBy(_tlog.placeRefCd.asc()).fetch();
        return Lists.transform(result, tuple -> {
            AggregateTemp temp = new AggregateTemp();
            temp.measurePlace = tuple.get(_tlog.placeRefCd);
            temp.min = tuple.get(min);
            temp.max = tuple.get(max);
            temp.avg = new BigDecimal(tuple.get(avg)).setScale(1, RoundingMode.HALF_UP).doubleValue();
            return temp;
        });

    }

    public void addLogMessage(long seqId, Date date, String logLevel, String message) {
        new SQLInsertClause(getConnection(), SQL_TEMPLATES, _alog)
                .set(_alog.date, date)
                .set(_alog.seqId, seqId)
                .set(_alog.logLevel, logLevel)
                .set(_alog.message, message)
                .execute();
    }

    public List<AppMsg> getLogs(Date from, Date to, int maxCount, List<String> levels) {
        NumberExpression<Integer> hour = _alog.date.hour().as("hour");
        NumberExpression<Integer> min = _alog.date.minute().as("min");
        List<Tuple> result = new MySQLQuery<StringSetting>(getConnection()).select(hour,
                min,
                _alog.message,
                _alog.id
        ).from(_alog).
                where(_alog.logLevel.in(levels)
                        .and(_alog.date.goe(from))
                        .and(_alog.date.loe(to))
                )
                .orderBy(_alog.date.desc()).limit(maxCount).fetch();
        return Lists.transform(result, tuple -> {
            AppMsg temp = new AppMsg();
            temp.id = tuple.get(_alog.id);
            temp.hour = String.format("%02d", tuple.get(hour));
            temp.minute = String.format("%02d", tuple.get(min));
            temp.message = tuple.get(_alog.message);
            return temp;
        });

    }

    public Double readLastValue(String placeRefCd) {
        return new MySQLQuery<StringSetting>(getConnection()).select(
                _tlog.value).from(_tlog).
                where(_tlog.placeRefCd.eq(placeRefCd)
                ).orderBy(_tlog.timestamp.desc()).fetchFirst();
    }

    public Double readPreviousValue(String placeRefCd, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class, leaveoutminutes, _tlog.timestamp);
        return new MySQLQuery<StringSetting>(getConnection()).select(
                _tlog.value.avg().as(_tlog.value)).from(_tlog).
                where(_tlog.placeRefCd.eq(placeRefCd)
                        .and(_tlog.timestamp.goe(calendar.getTime()))
                        .and(_tlog.timestamp.loe(date))
                ).orderBy(removeMinutes.desc()).fetchFirst();
    }

    public static class AggregateTemp {
        public String measurePlace;
        public double min;
        public double max;
        public double avg;
        public Double last;
    }

    public static class AppMsg {
        public long id;
        public String hour;
        public String minute;
        public String message;

    }
}
