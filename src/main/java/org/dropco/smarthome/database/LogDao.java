package org.dropco.smarthome.database;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.AppLog;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.database.querydsl.TemperatureLog;
import org.dropco.smarthome.database.querydsl.TemperatureLogHistory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class LogDao implements Dao {
    private Connection connection;


    private static final Template leaveoutminutes = TemplateFactory.DEFAULT.create("STR_TO_DATE(DATE_FORMAT({0},'%Y-%m-%d %H'),'%Y-%m-%d %H')");
    private static final Template keepDay = TemplateFactory.DEFAULT.create("DATE_FORMAT({0},'%Y-%m-%d')");
    protected static final SQLTemplates SQL_TEMPLATES = new MySQLTemplates();
    public static TemperatureLogHistory _tlogh = new TemperatureLogHistory("logh");
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
        MySQLQuery<Tuple> query1 = new MySQLQuery<StringSetting>(getConnection()).select(_tlog.placeRefCd,
                        removeMinutes.as(_tlog.timestamp),
                        _tlog.value.avg().as(_tlog.value)).from(_tlog).
                where(_tlog.placeRefCd.isNotNull()
                        .and(_tlog.timestamp.goe(from))
                        .and(_tlog.timestamp.loe(to))
                )
                .groupBy(removeMinutes, _tlog.placeRefCd);
        MySQLQuery<Tuple> query2 = new MySQLQuery<>(getConnection()).from(_tlogh).select(_tlogh.placeRefCd.as(_tlog.placeRefCd), _tlogh.asOfDate.as(_tlog.timestamp), _tlogh.average.as(_tlog.value))
                .where((_tlogh.asOfDate.goe(from)).and(_tlogh.asOfDate.loe(to)));
        return new MySQLQuery<Tuple>(getConnection()).union(query2, query1)
                .orderBy(new OrderSpecifier<>(Order.ASC, Expressions.asString(_tlog.timestamp.getMetadata().getName())), new OrderSpecifier<>(Order.ASC, Expressions.asString(_tlog.placeRefCd.getMetadata().getName())))
                .fetch();
    }

    public List<AggregateTemp> retrieveAggregatedTemperatures(Date from, Date to) {
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class, leaveoutminutes, _tlog.timestamp);
        MySQLQuery<Tuple> query1 = new MySQLQuery<StringSetting>(getConnection()).select(_tlog.placeRefCd,
                        removeMinutes,
                        _tlog.value.min().as("min"),
                        _tlog.value.max().as("max"),
                        _tlog.value.avg().as("avg")
                ).from(_tlog).
                where(_tlog.placeRefCd.isNotNull()
                        .and(_tlog.timestamp.goe(from))
                        .and(_tlog.timestamp.loe(to))
                )
                .groupBy(removeMinutes, _tlog.placeRefCd);
        MySQLQuery<Tuple> query2 = new MySQLQuery<>(getConnection()).from(_tlogh).select(_tlogh.placeRefCd.as(_tlog.placeRefCd), _tlogh.asOfDate, _tlogh.minimum.as("min"),_tlogh.maximum.as("max"),_tlogh.average.as("avg"))
                .where((_tlogh.asOfDate.goe(from)).and(_tlogh.asOfDate.loe(to)));

        Expression<Tuple> union = new MySQLQuery<Tuple>(getConnection()).union(query1, query2).as("x");
        SimplePath<String> placeRefCd = Expressions.path(String.class, "PLACE_REF_CD");
        NumberExpression<Double> min = Expressions.numberPath(Double.class, "min").min().as("minValue");
        NumberExpression<Double> max = Expressions.numberPath(Double.class, "max").max().as("maxValue");
        NumberExpression<Double> avg = Expressions.numberPath(Double.class, "avg").avg().as("avgValue");
        List<Tuple> result = new MySQLQuery<Tuple>(getConnection()).select(placeRefCd,
                        min,
                        max,
                        avg).from(union)
                .groupBy(placeRefCd)
                .fetch();
        return Lists.newArrayList(Iterables.transform(result, tuple -> {
            AggregateTemp temp = new AggregateTemp();
            temp.measurePlace = tuple.get(placeRefCd);
            temp.min = new BigDecimal(tuple.get(min)).setScale(1, RoundingMode.HALF_UP).doubleValue();
            temp.max = new BigDecimal(tuple.get(max)).setScale(1, RoundingMode.HALF_UP).doubleValue();
            temp.avg = new BigDecimal(tuple.get(avg)).setScale(1, RoundingMode.HALF_UP).doubleValue();
            return temp;
        }));

    }

    public Date retrieveLastDay() {
        return new MySQLQuery<Date>(getConnection())
                .select(_tlog.timestamp.min())
                .from(_tlog).fetchFirst();
    }


    public Iterable<HourAggregatedTemp> retrieveForDay(Date from) {
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class, leaveoutminutes, _tlog.timestamp);
        DateExpression<Date> keepDayExp = Expressions.dateTemplate(Date.class, keepDay, _tlog.timestamp);
        NumberExpression<Double> min = _tlog.value.min().as("min");
        NumberExpression<Double> max = _tlog.value.max().as("max");
        NumberExpression<Double> avg = _tlog.value.avg().as(_tlog.value);
        return Iterables.transform(new MySQLQuery<StringSetting>(getConnection())
                .select(_tlog.placeRefCd,
                        removeMinutes.as(_tlog.timestamp),
                        avg,
                        min,
                        max)
                .from(_tlog)
                .where(keepDayExp.eq(Expressions.dateTemplate(Date.class, keepDay, from)).and(_tlog.placeRefCd.isNotNull()))
                .groupBy(removeMinutes, _tlog.placeRefCd)
                .orderBy(_tlog.timestamp.asc(), _tlog.placeRefCd.asc()).fetch(),tuple->{
            HourAggregatedTemp t = new HourAggregatedTemp();
            t.min = tuple.get(min);
            t.max = tuple.get(max);
            t.avg = tuple.get(avg);
            t.asOf = tuple.get(_tlog.timestamp);
            t.measurePlace = tuple.get(_tlog.placeRefCd);
            return t;
        });
    }



    public List<String> listUnassignedDeviceIds() {
        return new MySQLQuery<StringSetting>(getConnection()).select(_tlog.devideId).distinct().from(_tlog).
                where(_tlog.placeRefCd.isNull()).fetch();
    }

    public void updateLogs(String deviceId, String refCd) {
        new SQLUpdateClause(getConnection(), SQL_TEMPLATES, _tlog)
                .set(_tlog.placeRefCd, refCd)
                .where(_tlog.devideId.eq(deviceId))
                .execute();
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
        Double val = new MySQLQuery<StringSetting>(getConnection()).select(
                        _tlog.value).from(_tlog).
                where(_tlog.placeRefCd.eq(placeRefCd)).orderBy(_tlog.timestamp.desc()).fetchFirst();
        if (val == null) val = new MySQLQuery<StringSetting>(getConnection()).select(
                        _tlogh.average).from(_tlogh).
                where(_tlogh.placeRefCd.eq(placeRefCd)).orderBy(_tlogh.asOfDate.desc()).fetchFirst();
        return new BigDecimal(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public Double readPreviousValue(String placeRefCd, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        DateExpression<Date> removeMinutes = Expressions.dateTemplate(Date.class, leaveoutminutes, _tlog.timestamp);
        Double val = new MySQLQuery<StringSetting>(getConnection()).select(
                        _tlog.value.avg().as(_tlog.value)).from(_tlog).
                where(_tlog.placeRefCd.eq(placeRefCd)
                        .and(_tlog.timestamp.goe(calendar.getTime()))
                        .and(_tlog.timestamp.loe(date))
                ).orderBy(removeMinutes.desc()).fetchFirst();
        if (val == null) {
            val = new MySQLQuery<StringSetting>(getConnection()).select(
                            _tlogh.average).from(_tlogh).
                    where(_tlogh.placeRefCd.eq(placeRefCd).and(_tlogh.asOfDate.goe(calendar.getTime()))
                            .and(_tlogh.asOfDate.loe(date))).orderBy(_tlogh.asOfDate.desc()).fetchFirst();
        }
        if (val == null) return 0.0;
        return new BigDecimal(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /***
     * Gets the connection
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void moveToHistory(LogDao.HourAggregatedTemp record) {
        new SQLInsertClause(getConnection(), SQL_TEMPLATES, _tlogh)
                .set(_tlogh.placeRefCd, record.measurePlace)
                .set(_tlogh.asOfDate, record.asOf)
                .set(_tlogh.average, record.avg)
                .set(_tlogh.minimum, record.min)
                .set(_tlogh.maximum, record.max)
                .execute();
    }

    public void deleteTempData(Date time) {
        DateExpression<Date> keepDayExp = Expressions.dateTemplate(Date.class, keepDay, _tlog.timestamp);
        new SQLDeleteClause(getConnection(), SQL_TEMPLATES, _tlog).where(keepDayExp.eq(Expressions.dateTemplate(Date.class, keepDay, time))).execute();
    }

    public void deleteOldLogs() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -14);
        new SQLDeleteClause(getConnection(), SQL_TEMPLATES, _alog).where(_alog.date.loe(calendar.getTime()).and(_alog.logLevel.eq("FINE"))).execute();
        calendar.add(Calendar.DAY_OF_YEAR, 14);
        calendar.add(Calendar.MONTH, -2);
        new SQLDeleteClause(getConnection(), SQL_TEMPLATES, _alog).where(_alog.date.loe(calendar.getTime())).execute();
    }

    public static class AggregateTemp {
        public String measurePlace;
        public double min;
        public double max;
        public double avg;
        public Double last;
        public int orderId;

        /***
         * Gets the orderId
         * @return
         */
        public int getOrderId() {
            return orderId;
        }
    }

    public static class AppMsg {
        public long id;
        public String hour;
        public String minute;
        public String message;

    }

    public static class HourAggregatedTemp{
        public String measurePlace;
        public Date asOf;
        public double min;
        public double max;
        public double avg;
    }
}
