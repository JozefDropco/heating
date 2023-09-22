package org.dropco.smarthome.stats;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.querydsl.Stats;
import org.dropco.smarthome.database.querydsl.StringSetting;

import java.sql.Connection;
import java.util.Date;
import java.util.List;


public class StatsDao implements Dao {
    private Connection connection;
    public static Stats _s = new Stats("s");

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

    public List<AggregatedStats> listAggregatedStats(Date from, Date to) {
        NumberTemplate<Long> diff = Expressions.numberTemplate(Long.class, secondsDiff, _s.fromDate, from, _s.toDate, to);
        NumberTemplate<Long> ticker = Expressions.numberTemplate(Long.class, tick, from, _s.fromDate);
        NumberExpression<Long> cnt = ticker.sum().as("cnt");
        NumberExpression<Long> sum = diff.sum().as("sum");
        List<Tuple> result = new MySQLQuery<StringSetting>(getConnection()).select(_s.name,
                        cnt,
                        sum
                ).from(_s).where(
                        diff.gt(0).and(
                                _s.fromDate.between(from, to)
                                        .or(_s.fromDate.lt(from).and(_s.toDate.isNull().or(_s.toDate.gt(from))))
                        ))
                .groupBy(_s.name)
                .orderBy(_s.name.asc()).fetch();
        return Lists.transform(result, tmp -> {
            AggregatedStats stats = new AggregatedStats();
            stats.name = tmp.get(_s.name);
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

    public static class AggregatedStats {
        public String name;
        public long count;
        public long secondsSum;
    }
}
