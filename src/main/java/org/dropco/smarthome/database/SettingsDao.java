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
    private boolean loaded;

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
        if (!loaded){
            loaded=true;
        loadStringCache();
        loadLongCache();
        loadDoubleCache();
        }
    }

    private void loadStringCache() {
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(STRING.all()).from(STRING).fetch();
        for (Tuple result: fetch){
            stringCacheMap.put(result.get(STRING.refCd),result.get(STRING.value));            
        }
    }

    private void loadLongCache() {
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(LONG.all()).from(LONG).where(condition).fetch();
        for (Tuple result: fetch){
            longCacheMap.put(result.get(LONG.refCd),result.get(LONG.value));
        }
    }

    private void loadDoubleCache() {
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(DOUBLE.all()).from(DOUBLE).where(condition).fetch();
        for (Tuple result: fetch){
            doubleCacheMap.put(result.get(DOUBLE.refCd),result.get(DOUBLE.value));
        }
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }

}
