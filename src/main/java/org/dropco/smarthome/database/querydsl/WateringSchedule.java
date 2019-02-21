package org.dropco.smarthome.database.querydsl;

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
public class WateringSchedule extends com.querydsl.sql.RelationalPathBase<WateringSchedule> {

    private static final long serialVersionUID = -332312148;

    public static final WateringSchedule WATERING_SCHEDULE = new WateringSchedule("WATERING_SCHEDULE");

    public final NumberPath<Long> id = createNumber("id",Long.class);
    public final NumberPath<Integer> month = createNumber("month",Integer.class);
    public final NumberPath<Integer> hour = createNumber("hour",Integer.class);
    public final NumberPath<Integer> minute = createNumber("minute",Integer.class);
    public final NumberPath<Integer> day = createNumber("day",Integer.class);
    public final StringPath timeZoneRefCd = createString("timeZoneRefCd");
    public final NumberPath<Long> timeInSeconds = createNumber("timeInSeconds",Long.class);

    public final com.querydsl.sql.PrimaryKey<WateringSchedule> posPk = createPrimaryKey(id);

    public WateringSchedule(String variable) {
        super(WateringSchedule.class, forVariable(variable), "HEATING", "WATERING_SCHEDULE");
        addMetadata();
    }

    public WateringSchedule(String schema, String variable) {
        super(WateringSchedule.class, forVariable(variable), schema, "WATERING_SCHEDULE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(month, ColumnMetadata.named("MONTH").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(hour, ColumnMetadata.named("HOUR").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(minute, ColumnMetadata.named("MINUTE").withIndex(4).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(day, ColumnMetadata.named("DAY").withIndex(5).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(timeInSeconds, ColumnMetadata.named("TIME_IN_SEC").withIndex(6).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(timeZoneRefCd, ColumnMetadata.named("TIME_ZONE_REF_CD").withIndex(7).ofType(Types.VARCHAR).withSize(50).withDigits(0).notNull());
    }

}

