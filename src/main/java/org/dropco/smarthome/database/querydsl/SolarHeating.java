package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.time.LocalTime;
import java.util.Date;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * Region is a Querydsl query type for Region
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SolarHeating extends com.querydsl.sql.RelationalPathBase<SolarHeating> {

    private static final long serialVersionUID = -332312148;

    public static final SolarHeating SOLAR_HEATING = new SolarHeating("SOLAR_HEATING");

    public final NumberPath<Integer> id = createNumber("id",Integer.class);
    public final NumberPath<Integer> day = createNumber("day",Integer.class);
    public final TimePath<LocalTime> fromTime = createTime("fromTime", LocalTime.class);
    public final TimePath<LocalTime> toTime = createTime("toTime", LocalTime.class);
    public final NumberPath<Double> threeWayValveStartDiff = createNumber("threeWayValveStartDiff",Double.class);
    public final NumberPath<Double> threeWayValveStopDiff = createNumber("threeWayValveStopDiff",Double.class);
    public final BooleanPath boilerBlocked= createBoolean("boilerBlocked");

    public final com.querydsl.sql.PrimaryKey<SolarHeating> pk = createPrimaryKey(id);

    public SolarHeating(String variable) {
        super(SolarHeating.class, forVariable(variable), "HEATING", "SOLAR_HEATING");
        addMetadata();
    }

    public SolarHeating(String schema, String variable) {
        super(SolarHeating.class, forVariable(variable), schema, "SOLAR_HEATING");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.NUMERIC).withSize(255).notNull());
        addMetadata(day, ColumnMetadata.named("DAY").withIndex(2).ofType(Types.NUMERIC).withSize(255).notNull());
        addMetadata(fromTime, ColumnMetadata.named("FROM_TIME").withIndex(3).ofType(Types.TIME).notNull());
        addMetadata(toTime, ColumnMetadata.named("TO_TIME").withIndex(4).ofType(Types.TIME).notNull());
        addMetadata(threeWayValveStartDiff, ColumnMetadata.named("3WAY_VALVE_DIFF_START").withIndex(5).ofType(Types.NUMERIC).withSize(255).notNull());
        addMetadata(threeWayValveStopDiff, ColumnMetadata.named("3WAY_VALVE_DIFF_STOP").withIndex(6).ofType(Types.NUMERIC).withSize(255).notNull());
        addMetadata(boilerBlocked, ColumnMetadata.named("BOILER_BLOCKED").withIndex(7).ofType(Types.NUMERIC).withSize(255).notNull());
    }

}

