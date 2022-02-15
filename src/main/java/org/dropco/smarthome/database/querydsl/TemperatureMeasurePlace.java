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
public class TemperatureMeasurePlace extends com.querydsl.sql.RelationalPathBase<TemperatureMeasurePlace> {

    private static final long serialVersionUID = -332312148;

    public static final TemperatureMeasurePlace TEMP_MEASURE_PLACE = new TemperatureMeasurePlace("TEMP_MEASURE_PLACE");

    public final StringPath placeRefCd = createString("placeRefCd");

    public final StringPath devideId = createString("deviceId");
    public final NumberPath<Integer> orderId = createNumber("orderId",Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<TemperatureMeasurePlace> pk = createPrimaryKey(placeRefCd);

    public TemperatureMeasurePlace(String variable) {
        super(TemperatureMeasurePlace.class, forVariable(variable), "HEATING", "TEMP_MEASURE_PLACE");
        addMetadata();
    }

    public TemperatureMeasurePlace(String schema, String variable) {
        super(TemperatureMeasurePlace.class, forVariable(variable), schema, "TEMP_MEASURE_PLACE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(placeRefCd, ColumnMetadata.named("PLACE_REF_CD").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(devideId, ColumnMetadata.named("DEVICE_ID").withIndex(2).ofType(Types.VARCHAR).withSize(255).withDigits(5).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(50).withDigits(5).notNull());
        addMetadata(orderId, ColumnMetadata.named("ORDER_ID").withIndex(4).ofType(Types.INTEGER).withSize(50).withDigits(5).notNull());
    }

}

