package org.dropco.smarthome.database;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.querydsl.core.Tuple;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.StringSetting;
import org.dropco.smarthome.dto.Constant;
import org.dropco.smarthome.dto.DoubleConstant;
import org.dropco.smarthome.dto.LongConstant;
import org.dropco.smarthome.dto.StringConstant;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dropco.smarthome.database.querydsl.DoubleSetting.DOUBLE;
import static org.dropco.smarthome.database.querydsl.LongSetting.LONG;
import static org.dropco.smarthome.database.querydsl.StringSetting.STRING;

public class SettingsDao {
    protected static final SQLTemplates SQL_TEMPLATES = new MySQLTemplates();
    private final Map<String, StringConstant> stringCacheMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, LongConstant> longCacheMap =  Collections.synchronizedMap(new HashMap<>());
    private final Map<String, DoubleConstant> doubleCacheMap =  Collections.synchronizedMap(new HashMap<>());
    private AtomicBoolean loaded =new AtomicBoolean();


    public String getString(String key) {
        updateIfNeeded();
        return Optional.ofNullable(stringCacheMap.get(key)).map(StringConstant::getValue).orElse(null);
    }


    public long getLong(String key) {
        updateIfNeeded();
        return Optional.ofNullable(longCacheMap.get(key)).map(LongConstant::getValue).orElse(null);
    }
    public Optional<LongConstant> getLongConst(String key) {
        updateIfNeeded();
        return Optional.ofNullable(longCacheMap.get(key));
    }
    public double getDouble(String key) {
        updateIfNeeded();
        return Optional.ofNullable(doubleCacheMap.get(key)).map(DoubleConstant::getValue).orElse(null);
    }

    private void updateIfNeeded() {
        if (!loaded.get()) {
            synchronized (SettingsDao.class) {
                boolean success = loaded.compareAndSet(false, true);
                if (!success) return;
                loadStringCache();
                loadLongCache();
                loadDoubleCache();
            }
        }
    }

    private void loadStringCache() {
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(STRING.all()).from(STRING).fetch();
        for (Tuple result : fetch) {
            stringCacheMap.put(result.get(STRING.refCd), toStringConst(result));
        }
    }

    private void loadLongCache() {
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(LONG.all()).from(LONG).fetch();
        for (Tuple result : fetch) {
            longCacheMap.put(result.get(LONG.refCd), toLongConst(result));
        }
    }

    private void loadDoubleCache() {
        List<Tuple> fetch = new MySQLQuery<StringSetting>(getConnection()).select(DOUBLE.all()).from(DOUBLE).fetch();
        for (Tuple result : fetch) {
            doubleCacheMap.put(result.get(DOUBLE.refCd), toDoubleConst(result));
        }
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }

    public void setLong(String key, long value) {
        long execute = new SQLUpdateClause(getConnection(), SQL_TEMPLATES, LONG)
                .set(LONG.modifiedTs, new Date())
                .set(LONG.value, value)
                .where(LONG.refCd.eq(key))
                .execute();
        if (execute == 1) longCacheMap.get(key).setValue(value);

    }

    public void setDouble(String key, double value) {
        long execute = new SQLUpdateClause(getConnection(), SQL_TEMPLATES, DOUBLE)
                .set(DOUBLE.modifiedTs, new Date())
                .set(DOUBLE.value, value)
                .where(DOUBLE.refCd.eq(key))
                .execute();
        if (execute == 1) doubleCacheMap.get(key).setValue(value);

    }

    public void setString(String key, String value) {
        long execute = new SQLUpdateClause(getConnection(), SQL_TEMPLATES, STRING)
                .set(STRING.modifiedTs, new Date())
                .set(STRING.value, value)
                .where(STRING.refCd.eq(key))
                .execute();
        if (execute == 1) stringCacheMap.get(key).setValue(value);

    }

    public List<Constant> readAll() {
        updateIfNeeded();
        ArrayList<Constant> constants = Lists.newArrayList();
        constants.addAll(longCacheMap.values());
        constants.addAll(doubleCacheMap.values());
        constants.addAll(stringCacheMap.values());
        return constants;
    }

    public boolean isLongModifiedAfter(String refCd, Date date) {
        return new MySQLQuery<StringSetting>(getConnection()).select(LONG.refCd).from(LONG).where(LONG.refCd.eq(refCd).and(LONG.modifiedTs.goe(date))).fetchCount() > 0;
    }


    private StringConstant toStringConst(Tuple result) {
        StringConstant constant = new StringConstant();
        constant.setValue(result.get(STRING.value));
        constant.setRefCd(result.get(STRING.refCd));
        constant.setDescription(result.get(STRING.description));
        constant.setGroup(result.get(STRING.group));
        constant.setLastModification(result.get(STRING.modifiedTs));
        constant.setValueType(result.get(STRING.valueType));
        constant.setConstantType("string");
        return constant;
    }

    private LongConstant toLongConst(Tuple result) {
        LongConstant constant = new LongConstant();
        constant.setValue(result.get(LONG.value));
        constant.setRefCd(result.get(LONG.refCd));
        constant.setDescription(result.get(LONG.description));
        constant.setGroup(result.get(LONG.group));
        constant.setLastModification(result.get(LONG.modifiedTs));
        constant.setValueType(result.get(LONG.valueType));
        constant.setConstantType("long");
        return constant;
    }

    private DoubleConstant toDoubleConst(Tuple result) {
        DoubleConstant constant = new DoubleConstant();
        constant.setValue(result.get(DOUBLE.value));
        constant.setRefCd(result.get(DOUBLE.refCd));
        constant.setDescription(result.get(DOUBLE.description));
        constant.setGroup(result.get(DOUBLE.group));
        constant.setLastModification(result.get(DOUBLE.modifiedTs));
        constant.setValueType(result.get(DOUBLE.valueType));
        constant.setConstantType("double");
        return constant;
    }

    public void updateLongConstant(LongConstant longConstant) {
        long execute = new SQLUpdateClause(getConnection(), SQL_TEMPLATES, LONG)
                .set(LONG.modifiedTs, new Date())
                .set(LONG.value, longConstant.getValue())
                .set(LONG.description, longConstant.getDescription())
                .set(LONG.group, longConstant.getGroup())
                .set(LONG.valueType, longConstant.getValueType())
                .where(LONG.refCd.eq(longConstant.getRefCd()))
                .execute();
        if (execute == 1) longCacheMap.put(longConstant.getRefCd(),longConstant);

    }

    public void updateStringConstant(StringConstant stringConstant) {
        long execute = new SQLUpdateClause(getConnection(), SQL_TEMPLATES, STRING)
                .set(STRING.modifiedTs, new Date())
                .set(STRING.value, stringConstant.getValue())
                .set(STRING.description, stringConstant.getDescription())
                .set(STRING.group, stringConstant.getGroup())
                .set(STRING.valueType, stringConstant.getValueType())
                .where(STRING.refCd.eq(stringConstant.getRefCd()))
                .execute();
        if (execute == 1) stringCacheMap.put(stringConstant.getRefCd(),stringConstant);
    }

    public void updateDoubleConstant(DoubleConstant doubleConst) {
        long execute = new SQLUpdateClause(getConnection(), SQL_TEMPLATES, DOUBLE)
                .set(DOUBLE.modifiedTs, new Date())
                .set(DOUBLE.value, doubleConst.getValue())
                .set(DOUBLE.description, doubleConst.getDescription())
                .set(DOUBLE.group, doubleConst.getGroup())
                .set(DOUBLE.valueType, doubleConst.getValueType())
                .where(DOUBLE.refCd.eq(doubleConst.getRefCd()))
                .execute();
        if (execute == 1) doubleCacheMap.put(doubleConst.getRefCd(),doubleConst);
    }
}
