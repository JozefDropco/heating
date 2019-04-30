package org.dropco.smarthome.watering2.db;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.DBConnection;
import org.dropco.smarthome.database.querydsl.SolarPosition;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.dropco.smarthome.database.querydsl.WateringSchedule.WATERING_SCHEDULE;

public class WateringDao {
    private static final Template toDate = TemplateFactory.DEFAULT.create("STR_TO_DATE(CONCAT({0},'-',{1},'-',{2},' ',{3},':',{4},':',{5}), '%Y-%c-%e %k:%i:%s')");

    public List<WateringRetry> getTodayRecords(Calendar cal) {
        Expression<?>[] list = new Expression[]{WATERING_SCHEDULE.zoneRefCd, WATERING_SCHEDULE.timeInSeconds, WATERING_SCHEDULE.hour, WATERING_SCHEDULE.minute, WATERING_SCHEDULE.month, WATERING_SCHEDULE.day};
        DateTemplate<Date> dateTemplate = Expressions.dateTemplate(Date.class, toDate, cal.get(Calendar.YEAR), WATERING_SCHEDULE.month, WATERING_SCHEDULE.day, WATERING_SCHEDULE.hour, WATERING_SCHEDULE.minute, 0);
        Date currentTime = cal.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        DateTemplate<Date> dateTemplateNextDay = Expressions.dateTemplate(Date.class, toDate, calendar.get(Calendar.YEAR), WATERING_SCHEDULE.month, WATERING_SCHEDULE.day, WATERING_SCHEDULE.hour, WATERING_SCHEDULE.minute, 0);
        BooleanExpression whereCond = dateTemplate.goe(currentTime).and(dateTemplateNextDay.loe(calendar.getTime()));
        List<Tuple> lst = new MySQLQuery<SolarPosition>(getConnection()).select(list).from(WATERING_SCHEDULE).where(whereCond).orderBy(dateTemplate.asc()).
                fetch();
        return FluentIterable.from(lst).transform(tuple -> toRecord(tuple)).toList();
    }

    public Set<String> getAllZones() {
        return Sets.newHashSet(new MySQLQuery<SolarPosition>(getConnection()).select(WATERING_SCHEDULE.zoneRefCd).from(WATERING_SCHEDULE).distinct().fetch());
    }
    private WateringRetry toRecord(Tuple tuple) {
        WateringRetry record = new WateringRetry();
        record.setHour(tuple.get(WATERING_SCHEDULE.hour));
        record.setMonth(tuple.get(WATERING_SCHEDULE.month));
        record.setMinute(tuple.get(WATERING_SCHEDULE.minute));
        record.setDay(tuple.get(WATERING_SCHEDULE.day));
        record.setTimeInSeconds(tuple.get(WATERING_SCHEDULE.timeInSeconds));
        record.setZoneRefCode(tuple.get(WATERING_SCHEDULE.zoneRefCd));
        return record;
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }

    public List<WateringRecord> getWateringRecords() {
        return null;
    }

    public List<WateringRetry> getRetries() {
        return null;
    }

    public void deleteRetry(String currentZone) {

    }

    public WateringRecord readRecord(long nextRetryId) {
        return null;
    }
}
