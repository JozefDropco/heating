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

    private static final Template secondsDiff = TemplateFactory.DEFAULT.create("TIMESTAMPDIFF(SECOND,{0},IF({1}<{2},{1},{2}))");

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

    public List<AggregatedStats> listAggregatedStats(Date from, Date to){
        NumberTemplate<Long> diff = Expressions.numberTemplate(Long.class, secondsDiff, _s.fromDate,_s.toDate,to);
        NumberExpression<Long> cnt = _s.count().as("cnt");
        NumberExpression<Long> sum = diff.sum().as("sum");
        List<Tuple> result = new MySQLQuery<StringSetting>(getConnection()).select(_s.name,
                cnt,
                sum
        ).from(_s).where(_s.fromDate.goe(from).and(_s.fromDate.loe(to)))
                .groupBy(_s.name)
                .orderBy(_s.name.asc()).fetch();
        return Lists.transform(result,tmp->{
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

    public static class AggregatedStats {
        public String name;
        public long count;
        public long secondsSum;
    }
}
