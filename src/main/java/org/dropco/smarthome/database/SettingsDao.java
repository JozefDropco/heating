package org.dropco.smarthome.database;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.StringSetting;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dropco.smarthome.database.querydsl.DoubleSetting.DOUBLE;
import static org.dropco.smarthome.database.querydsl.LongSetting.LONG;
import static org.dropco.smarthome.database.querydsl.StringSetting.STRING;

public class SettingsDao {
    private Map<String, String> stringCacheMap = new HashMap<>();
    private Map<String, Long> longCacheMap = new HashMap<>();
    private Map<String, Double> doubleCacheMap = new HashMap<>();
    private Date lastUpdatedDateString;
    private Date lastUpdatedDateLong;

    private Date lastUpdatedDateDouble;


    public String getString(String key) {
        updateIfNeeded();
        return stringCacheMap.get(key);
    }


    public long getLong(String key) {
        updateIfNeeded();
        return longCacheMap.get(key);
    }

    public double getDouble(String key) {
        updateIfNeeded();
        return doubleCacheMap.get(key);
    }

    private void updateIfNeeded() {
        loadStringCache();
        loadLongCache();
        loadDoubleCache();
    }

    private void loadStringCache() {
        Predicate condition;
        if (lastUpdatedDateString==null){
            condition=STRING.modifiedTs.eq(STRING.modifiedTs);
        } else {
            condition = STRING.modifiedTs.after(lastUpdatedDateString);
        }
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(STRING.all()).from(STRING).where(condition).fetch();
        for (Tuple result: fetch){
            stringCacheMap.put(result.get(STRING.refCd),result.get(STRING.value));
            Date date = result.get(STRING.modifiedTs);
            if (lastUpdatedDateString==null || date.after(lastUpdatedDateString))
            lastUpdatedDateString = date;
        }
    }

    private void loadLongCache() {
        Predicate condition;
        List<Tuple> fetch;
        if (lastUpdatedDateLong==null){
            condition=LONG.modifiedTs.eq(LONG.modifiedTs);
        } else {
            condition = LONG.modifiedTs.after(lastUpdatedDateLong);
        }
        fetch = new MySQLQuery<StringSetting>(getConnection()).select(LONG.all()).from(LONG).where(condition).fetch();
        for (Tuple result: fetch){
            longCacheMap.put(result.get(LONG.refCd),result.get(LONG.value));
            Date date = result.get(LONG.modifiedTs);
            if (lastUpdatedDateLong==null || date.after(lastUpdatedDateLong))
                lastUpdatedDateLong= date;
        }
    }

    private void loadDoubleCache() {
        Predicate condition;
        List<Tuple> fetch;
        if (lastUpdatedDateDouble==null){
            condition=DOUBLE.modifiedTs.eq(DOUBLE.modifiedTs);
        } else {
            condition = DOUBLE.modifiedTs.after(lastUpdatedDateDouble);
        }
        fetch = new MySQLQuery<StringSetting>(getConnection()).select(DOUBLE.all()).from(DOUBLE).where(condition).fetch();
        for (Tuple result: fetch){
            doubleCacheMap.put(result.get(DOUBLE.refCd),result.get(DOUBLE.value));
            Date date = result.get(DOUBLE.modifiedTs);
            if (lastUpdatedDateDouble=null || date.after(lastUpdatedDateDouble))
                lastUpdatedDateDouble = date;
        }
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }

}
