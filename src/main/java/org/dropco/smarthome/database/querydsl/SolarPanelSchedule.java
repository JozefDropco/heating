package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * Region is a Querydsl query type for Region
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SolarPanelSchedule extends com.querydsl.sql.RelationalPathBase<SolarPanelSchedule> {

    private static final long serialVersionUID = -332312148;

    public static final SolarPanelSchedule SOLAR_SCHEDULE = new SolarPanelSchedule("SOLAR_SCHEDULE");

    public final NumberPath<Long> id = createNumber("id",Long.class);
    public final NumberPath<Integer> month = createNumber("month",Integer.class);
    public final NumberPath<Integer> hour = createNumber("hour",Integer.class);
    public final NumberPath<Integer> minute = createNumber("minute",Integer.class);
    public final NumberPath<Integer> day = createNumber("day",Integer.class);
    public final NumberPath<Long> position = createNumber("position",Long.class);

    public final com.querydsl.sql.PrimaryKey<SolarPanelSchedule> posPk = createPrimaryKey(id);

    public SolarPanelSchedule(String variable) {
        super(SolarPanelSchedule.class, forVariable(variable), "HEATING", "SOLAR_SCHEDULE");
        addMetadata();
    }

    public SolarPanelSchedule(String schema, String variable) {
        super(SolarPanelSchedule.class, forVariable(variable), schema, "SOLAR_SCHEDULE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(month, ColumnMetadata.named("MONTH").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(hour, ColumnMetadata.named("HOUR").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(minute, ColumnMetadata.named("MINUTE").withIndex(4).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(day, ColumnMetadata.named("DAY").withIndex(5).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(position, ColumnMetadata.named("POSITION").withIndex(6).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
    }

}

