package org.dropco.smarthome.solar;

import com.google.common.collect.FluentIterable;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.DBConnection;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.querydsl.SolarPosition;
import org.dropco.smarthome.web.SolarWebService;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.dropco.smarthome.database.querydsl.SolarPanelSchedule.SOLAR_SCHEDULE;
import static org.dropco.smarthome.database.querydsl.SolarPosition.SOLAR_POSITION;
import static org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace.TEMP_MEASURE_PLACE;

public class SolarSystemDao {
    protected static final String SOLAR_PANEL_DELAY = "SOLAR_PANEL_DELAY";
    protected static final SQLTemplates DEFAULT = new MySQLTemplates();
    private SettingsDao settingsDao;
    private static final Template toDate = TemplateFactory.DEFAULT.create("STR_TO_DATE(CONCAT({0},'-',{1},'-',{2},' ',{3},':',{4},':',{5}), '%Y-%c-%e %k:%i:%s')");

    public SolarSystemDao(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    public SolarPanelPosition getLastKnownPosition() {
        return getPositionById(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_REF_CD));
    }

    SolarPanelPosition getPositionById(long id) {
        Tuple tuple = new MySQLQuery<SolarPosition>(getConnection()).select(SOLAR_POSITION.all()).from(SOLAR_POSITION).where(SOLAR_POSITION.id.eq(id)).fetchFirst();
        SolarPanelPosition solarPanelPosition = new SolarPanelPosition();
        solarPanelPosition.setVerticalPositionInSeconds(tuple.get(SOLAR_POSITION.verticalPosition));
        solarPanelPosition.setHorizontalPositionInSeconds(tuple.get(SOLAR_POSITION.horizontalPosition));
        return solarPanelPosition;
    }

    public SolarPanelPosition getStrongWindPosition() {
        return getPositionById(settingsDao.getLong(SolarSystemRefCode.STRONG_WIND_POSITION_REF_CD));
    }

    public SolarPanelPosition getOverheatedPosition() {
        return getPositionById(settingsDao.getLong(SolarSystemRefCode.OVERHEAT_POSITION_REF_CD));
    }

    public void updateLastKnownPosition(SolarPanelPosition currentPosition) {
        new SQLUpdateClause(getConnection(), DEFAULT, SOLAR_POSITION)
                .set(SOLAR_POSITION.verticalPosition, currentPosition.getVerticalPositionInSeconds())
                .set(SOLAR_POSITION.horizontalPosition, currentPosition.getHorizontalPositionInSeconds())
                .where(SOLAR_POSITION.id.eq(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_REF_CD)))
                .execute();
    }


    public List<SolarPanelStepRecord> getTodayRecords(Calendar cal) {
        Expression<?>[] list = new Expression[]{SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day,SOLAR_SCHEDULE.ignoreDayLight};
        DateTemplate<Date> dateTemplate = Expressions.dateTemplate(Date.class, toDate, cal.get(Calendar.YEAR), SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, 0);
        Date currentTime = cal.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.SECOND, -1);
        DateTemplate<Date> dateTemplateNextDay = Expressions.dateTemplate(Date.class, toDate, calendar.get(Calendar.YEAR), SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, 0);
        BooleanExpression whereCond = dateTemplate.goe(currentTime).and(dateTemplateNextDay.loe(calendar.getTime()));
        List<Tuple> lst = new MySQLQuery<SolarPosition>(getConnection()).select(list).from(SOLAR_SCHEDULE).join(SOLAR_POSITION).on(SOLAR_SCHEDULE.position.eq(SOLAR_POSITION.id)).where(whereCond).orderBy(dateTemplate.asc()).
                fetch();
        return FluentIterable.from(lst).transform(tuple -> toRecord(tuple)).toList();
    }

    public List<SolarPanelStepRecord> getMonthRecords(int month) {
        Expression<?>[] list = new Expression[]{SOLAR_SCHEDULE.month,SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, SOLAR_SCHEDULE.ignoreDayLight};
        List<Tuple> lst = new MySQLQuery<SolarPosition>(getConnection()).select(list).from(SOLAR_SCHEDULE).join(SOLAR_POSITION).on(SOLAR_SCHEDULE.position.eq(SOLAR_POSITION.id))
                .where(SOLAR_SCHEDULE.month.eq(month)).groupBy(SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, SOLAR_SCHEDULE.month,SOLAR_SCHEDULE.ignoreDayLight).
                orderBy(SOLAR_SCHEDULE.hour.asc(),SOLAR_SCHEDULE.minute.asc()).fetch();
        return FluentIterable.from(lst).transform(tuple -> toRecord(tuple)).toList();
    }

    private SolarPanelStepRecord toRecord(Tuple tuple) {
        SolarPanelStepRecord record = new SolarPanelStepRecord();
        record.setHour(tuple.get(SOLAR_SCHEDULE.hour));
        record.setMonth(tuple.get(SOLAR_SCHEDULE.month));
        record.setMinute(tuple.get(SOLAR_SCHEDULE.minute));
        record.setDay(Optional.ofNullable(tuple.get(SOLAR_SCHEDULE.day)).orElse(1));
        record.setIgnoreDaylight(Optional.ofNullable(tuple.get(SOLAR_SCHEDULE.ignoreDayLight)).orElse(false));
        SolarPanelPosition position = new SolarPanelPosition();
        position.setHorizontalPositionInSeconds(tuple.get(SOLAR_POSITION.horizontalPosition));
        position.setVerticalPositionInSeconds(tuple.get(SOLAR_POSITION.verticalPosition));
        record.setPanelPosition(position);
        return record;
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }

    public SolarPanelStepRecord getRecentRecord(Calendar instance) {
        Expression<?>[] list = new Expression[]{SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day,SOLAR_SCHEDULE.ignoreDayLight};
        DateTemplate<Date> dateTemplate = Expressions.dateTemplate(Date.class, toDate, instance.get(Calendar.YEAR), SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, 0);
        BooleanExpression where = dateTemplate.loe(instance.getTime());
        Tuple tuple = new MySQLQuery<SolarPosition>(getConnection()).select(list).from(SOLAR_SCHEDULE).join(SOLAR_POSITION).on(SOLAR_SCHEDULE.position.eq(SOLAR_POSITION.id)).where(where)
                .orderBy(dateTemplate.desc()).
                        fetchFirst();
        return toRecord(tuple);
    }

    public long getDelay() {
        return settingsDao.getLong(SOLAR_PANEL_DELAY);
    }

    public void updateForMonth(SolarWebService.SolarDTO dto, int month) {
        new SQLUpdateClause(getConnection(), DEFAULT, SOLAR_SCHEDULE)
                .set(SOLAR_SCHEDULE.ignoreDayLight, dto.getIgnore())
                .where(SOLAR_SCHEDULE.month.eq(month).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))
                .execute();
        new SQLUpdateClause(getConnection(), DEFAULT, SOLAR_POSITION)
                .set(SOLAR_POSITION.horizontalPosition, dto.getHor())
                .set(SOLAR_POSITION.verticalPosition, dto.getVert())
                .where(SOLAR_POSITION.id.in(new MySQLQuery<>(getConnection()).select(SOLAR_SCHEDULE.position).from(SOLAR_SCHEDULE)
                        .where(SOLAR_SCHEDULE.month.eq(month).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))))
                .execute();
    }

    public void updateForDate(SolarWebService.SolarDTO dto, Calendar cal) {
        new SQLUpdateClause(getConnection(), DEFAULT, SOLAR_SCHEDULE)
                .set(SOLAR_SCHEDULE.ignoreDayLight, dto.getIgnore())
                .where(SOLAR_SCHEDULE.month.eq(cal.get(Calendar.MONTH)+1).and(SOLAR_SCHEDULE.day.eq(cal.get(Calendar.DAY_OF_MONTH))).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))
                .execute();
        new SQLUpdateClause(getConnection(), DEFAULT, SOLAR_POSITION)
                .set(SOLAR_POSITION.horizontalPosition, dto.getHor())
                .set(SOLAR_POSITION.verticalPosition, dto.getVert())
                .where(SOLAR_POSITION.id.in(new MySQLQuery<>(getConnection()).select(SOLAR_SCHEDULE.position).from(SOLAR_SCHEDULE)
                        .where(SOLAR_SCHEDULE.month.eq(cal.get(Calendar.MONTH)+1).and(SOLAR_SCHEDULE.day.eq(cal.get(Calendar.DAY_OF_MONTH))).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))))
                .execute();
    }

    public void deleteForDate(SolarWebService.SolarDTO dto, Calendar cal) {
        new SQLDeleteClause(getConnection(), DEFAULT, SOLAR_POSITION)
                .where(SOLAR_POSITION.id.in(new MySQLQuery<>(getConnection()).select(SOLAR_SCHEDULE.position).from(SOLAR_SCHEDULE)
                        .where(SOLAR_SCHEDULE.month.eq(cal.get(Calendar.MONTH)+1).and(SOLAR_SCHEDULE.day.eq(cal.get(Calendar.DAY_OF_MONTH))).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))))
                .execute();
        new SQLDeleteClause(getConnection(), DEFAULT, SOLAR_SCHEDULE)
                .where(SOLAR_SCHEDULE.month.eq(cal.get(Calendar.MONTH)+1).and(SOLAR_SCHEDULE.day.eq(cal.get(Calendar.DAY_OF_MONTH))).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))
                .execute();
    }

    public void deleteForMonth(SolarWebService.SolarDTO dto, int month) {
        new SQLDeleteClause(getConnection(), DEFAULT, SOLAR_POSITION)
                .where(SOLAR_POSITION.id.in(new MySQLQuery<>(getConnection()).select(SOLAR_SCHEDULE.position).from(SOLAR_SCHEDULE)
                        .where(SOLAR_SCHEDULE.month.eq(month).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))))
                .execute();
        new SQLDeleteClause(getConnection(), DEFAULT, SOLAR_SCHEDULE)
                .where(SOLAR_SCHEDULE.month.eq(month).and(SOLAR_SCHEDULE.hour.eq(dto.getHour())).and(SOLAR_SCHEDULE.minute.eq(dto.getMinute())))
                .execute();
    }
}
