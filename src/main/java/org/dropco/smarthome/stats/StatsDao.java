package org.dropco.smarthome.stats;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.querydsl.Stats;
import org.dropco.smarthome.database.querydsl.StatsHistory;
import org.dropco.smarthome.database.querydsl.StringSetting;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class StatsDao implements Dao {
    private Connection connection;
    protected static final SQLTemplates SQL_TEMPLATES = new MySQLTemplates();
    public static Stats _s = new Stats("s");
    public static StatsHistory _sh = new StatsHistory("sh");
    private static final Template keepDay = TemplateFactory.DEFAULT.create("DATE_FORMAT({0},'%Y-%m-%d')");

    private static final Template secondsDiff = TemplateFactory.DEFAULT.create("TIMESTAMPDIFF(SECOND,IF({0}>{1},{0},{1}),IF({2} IS NOT NULL and {2}<{3},{2},{3}))");
    private static final Template tick = TemplateFactory.DEFAULT.create("IF({0}>{1},0,1)");

    public void markAllFinished(Date date) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, _s)
                .set(_s.toDate, date)
                .where(_s.toDate.isNull())
                .execute();
    }

    public long addEntry(String name, Date date) {
        return new SQLInsertClause(getConnection(), SQLTemplates.DEFAULT, _s)
                .set(_s.name, name)
                .set(_s.fromDate, date)
                .executeWithKey(_s.id);
    }

    public void finishEntry(long previousId, Date date) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, _s)
                .set(_s.toDate, date)
                .where(_s.id.eq(previousId))
                .execute();
    }

    public List<AggregatedStats> listAggregatedStats(Date forDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(forDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date start = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        return listAggregatedStats(start, calendar.getTime());
    }

    public List<AggregatedStats> listAggregatedStats(Date from, Date to) {
        NumberTemplate<Long> diff = Expressions.numberTemplate(Long.class, secondsDiff, _s.fromDate, from, _s.toDate, to);
        NumberTemplate<Long> ticker = Expressions.numberTemplate(Long.class, tick, from, _s.fromDate);
        NumberExpression<Long> cnt = ticker.sum().as("cnt");
        NumberExpression<Long> sum = diff.sum().as("sum");
        StringExpression name = _s.name.as("name");
        MySQLQuery<Tuple> query1 = new MySQLQuery<StringSetting>(getConnection()).select(name,
                        cnt,
                        sum
                ).from(_s).where(
                        diff.gt(0).and(
                                _s.fromDate.between(from, to)
                                        .or(_s.fromDate.lt(from).and(_s.toDate.isNull().or(_s.toDate.gt(from))))
                        ))
                .groupBy(_s.name)
                .orderBy(_s.name.asc());
        MySQLQuery<Tuple> query2 = new MySQLQuery<>(getConnection()).from(_sh).select(_sh.name.as("name"), _sh.count.sum().as("cnt"),_sh.secondsSum.sum().as("sum"))
                .where((_sh.asOfDate.goe(from)).and(_sh.asOfDate.loe(to))).groupBy(_sh.name);
        List<Tuple> result = new MySQLQuery<Tuple>(getConnection()).union(query1, query2)
                .groupBy(Expressions.asString(_s.name.getMetadata().getName()))
                .orderBy(new OrderSpecifier<>(Order.ASC, Expressions.asString(_s.name.getMetadata().getName())))
                .fetch();
        return Lists.transform(result, tmp -> {
            AggregatedStats stats = new AggregatedStats();
            stats.name = tmp.get(name);
            stats.count = tmp.get(cnt);
            stats.secondsSum = tmp.get(sum);
            return stats;
        });
    }


    private Connection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Date retrieveLastDay() {
        return new MySQLQuery<Date>(getConnection())
                .select(_s.fromDate.min())
                .from(_s).fetchFirst();
    }

    public void moveToHistory(AggregatedStats record, Date asOfDate) {
        new SQLInsertClause(getConnection(), SQL_TEMPLATES, _sh)
                .set(_sh.name, record.name)
                .set(_sh.asOfDate, Expressions.dateTemplate(Date.class, keepDay, asOfDate))
                .set(_sh.count, record.count)
                .set(_sh.secondsSum, record.secondsSum)
                .execute();
    }

    public void deleteTempData(Date time) {
        DateExpression<Date> keepDayExp = Expressions.dateTemplate(Date.class, keepDay, _s.toDate);
        new SQLDeleteClause(getConnection(), SQL_TEMPLATES, _s).where(keepDayExp.eq(Expressions.dateTemplate(Date.class, keepDay, time))).execute();

    }


    public static class AggregatedStats {
        public String name;
        public long count;
        public long secondsSum;
    }
}
