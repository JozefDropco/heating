package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * Region is a Querydsl query type for Region
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class WateringZone extends com.querydsl.sql.RelationalPathBase<WateringZone> {

    private static final long serialVersionUID = -332312148;

    public static final WateringZone WATERING_ZONE = new WateringZone("WATERING_ZONE");

    public final NumberPath<Long> id = createNumber("id",Long.class);
    public final NumberPath<Integer> modulo = createNumber("modulo",Integer.class);
    public final NumberPath<Integer> reminder = createNumber("reminder",Integer.class);
    public final NumberPath<Integer> hour = createNumber("hour",Integer.class);
    public final NumberPath<Integer> retryHour = createNumber("retryHour",Integer.class);
    public final NumberPath<Integer> minute = createNumber("minute",Integer.class);
    public final NumberPath<Integer> retryMinute = createNumber("retryMinute",Integer.class);
    public final StringPath pinZoneRefCd = createString("pinZoneRefCd");
    public final NumberPath<Long> timeInSeconds = createNumber("timeInSeconds",Long.class);
    public final BooleanPath active = createBoolean("active");
    public final BooleanPath continuous = createBoolean("continuous");

    public final com.querydsl.sql.PrimaryKey<WateringZone> posPk = createPrimaryKey(id);

    public WateringZone(String variable) {
        super(WateringZone.class, forVariable(variable), "HEATING", "WATERING_ZONE");
        addMetadata();
    }

    public WateringZone(String schema, String variable) {
        super(WateringZone.class, forVariable(variable), schema, "WATERING_ZONE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(hour, ColumnMetadata.named("HOUR").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(retryHour, ColumnMetadata.named("RETRY_HOUR").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(minute, ColumnMetadata.named("MINUTE").withIndex(4).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(retryMinute, ColumnMetadata.named("RETRY_MINUTE").withIndex(4).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(modulo, ColumnMetadata.named("MODULO").withIndex(5).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(reminder, ColumnMetadata.named("REMINDER").withIndex(8).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(timeInSeconds, ColumnMetadata.named("TIME_IN_SEC").withIndex(6).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(pinZoneRefCd, ColumnMetadata.named("PIN_ZONE_REF_CD").withIndex(7).ofType(Types.VARCHAR).withSize(50).withDigits(0).notNull());
        addMetadata(active, ColumnMetadata.named("ENABLED").withIndex(9).ofType(Types.BOOLEAN).withSize(50).withDigits(0).notNull());
        addMetadata(continuous, ColumnMetadata.named("CONTINOUOUS").withIndex(10).ofType(Types.BOOLEAN).notNull());
    }

}

