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
public class TemperatureLogHistory extends com.querydsl.sql.RelationalPathBase<TemperatureLogHistory> {

    private static final long serialVersionUID = -332312148;

    public static final TemperatureLogHistory TEMPERATURE_LOG_HISTORY = new TemperatureLogHistory("TEMPERATURE_LOG_HISTORY");

    public final StringPath placeRefCd = createString("placeRefCd");


    public final NumberPath<Double> value = createNumber("value",Double.class);

    public final DateTimePath<Date> asOfDate = createDateTime("asOfDate", Date.class);


    public final com.querydsl.sql.PrimaryKey<TemperatureLogHistory> pk = createPrimaryKey(asOfDate,placeRefCd);

    public TemperatureLogHistory(String variable) {
        super(TemperatureLogHistory.class, forVariable(variable), "HEATING", "TEMPERATURE_LOG_HISTORY");
        addMetadata();
    }

    public TemperatureLogHistory(String schema, String variable) {
        super(TemperatureLogHistory.class, forVariable(variable), schema, "TEMPERATURE_LOG_HISTORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(placeRefCd, ColumnMetadata.named("PLACE_REF_CD").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("TEMP_VALUE").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(5).notNull());
        addMetadata(asOfDate, ColumnMetadata.named("AS_OF_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
    }

}

