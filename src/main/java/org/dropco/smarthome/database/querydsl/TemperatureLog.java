package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;
import java.util.Date;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * Region is a Querydsl query type for Region
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class TemperatureLog extends com.querydsl.sql.RelationalPathBase<TemperatureLog> {

    private static final long serialVersionUID = -332312148;

    public static final TemperatureLog TEMPERATURE_LOG = new TemperatureLog("TEMPERATURE_LOG");

    public final StringPath placeRefCd = createString("placeRefCd");

    public final StringPath devideId = createString("deviceId");

    public final NumberPath<Double> value = createNumber("value",Double.class);

    public final DateTimePath<Date> timestamp = createDateTime("timestamp", Date.class);


    public final com.querydsl.sql.PrimaryKey<TemperatureLog> tV21RefRegionPk = createPrimaryKey(devideId,timestamp);

    public TemperatureLog(String variable) {
        super(TemperatureLog.class, forVariable(variable), "HEATING", "TEMPERATURE_LOG");
        addMetadata();
    }

    public TemperatureLog(String schema, String variable) {
        super(TemperatureLog.class, forVariable(variable), schema, "TEMPERATURE_LOG");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(placeRefCd, ColumnMetadata.named("PLACE_REF_CD").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(devideId, ColumnMetadata.named("DEVICE_ID").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(5).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(5).notNull());
        addMetadata(timestamp, ColumnMetadata.named("MODIFIED_TS").withIndex(4).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
    }

}

