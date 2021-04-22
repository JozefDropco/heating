package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.BooleanPath;
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

    public final NumberPath<Integer> month = createNumber("month",Integer.class);
    public final NumberPath<Integer> horizontalStep = createNumber("horizontalStep",Integer.class);
    public final NumberPath<Integer> verticalStep = createNumber("verticalStep",Integer.class);

    public final NumberPath<Integer> sunRiseHour = createNumber("sunRiseHour",Integer.class);
    public final NumberPath<Integer> sunRiseMinute = createNumber("sunRiseMinute",Integer.class);
    public final NumberPath<Integer> sunRiseAbsPosHor = createNumber("sunRiseAbsPosHor",Integer.class);
    public final NumberPath<Integer> sunRiseAbsPosVert = createNumber("sunRiseAbsPosVert",Integer.class);

    public final NumberPath<Integer> sunSetHour = createNumber("sunSetHour",Integer.class);
    public final NumberPath<Integer> sunSetMinute = createNumber("sunSetMinute",Integer.class);
    public final NumberPath<Integer> sunSetAbsPosHor = createNumber("sunSetAbsPosHor",Integer.class);
    public final NumberPath<Integer> sunSetAbsPosVert = createNumber("sunSetAbsPosVert",Integer.class);

    public final com.querydsl.sql.PrimaryKey<SolarPanelSchedule> posPk = createPrimaryKey(month);

    public SolarPanelSchedule(String variable) {
        super(SolarPanelSchedule.class, forVariable(variable), "HEATING", "SOLAR_SCHEDULE");
        addMetadata();
    }

    public SolarPanelSchedule(String schema, String variable) {
        super(SolarPanelSchedule.class, forVariable(variable), schema, "SOLAR_SCHEDULE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(month, ColumnMetadata.named("MONTH").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(horizontalStep, ColumnMetadata.named("HORIZONTAL_STEP").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(verticalStep, ColumnMetadata.named("VERTICAL_STEP").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());

        addMetadata(sunRiseHour, ColumnMetadata.named("SUN_RISE_HOUR").withIndex(4).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(sunRiseMinute, ColumnMetadata.named("SUN_RISE_MINUTE").withIndex(5).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(sunRiseAbsPosHor, ColumnMetadata.named("SUN_RISE_ABS_POS_HOR").withIndex(6).ofType(Types.NUMERIC).notNull());
        addMetadata(sunRiseAbsPosVert, ColumnMetadata.named("SUN_RISE_ABS_POS_VERT").withIndex(7).ofType(Types.NUMERIC).notNull());

        addMetadata(sunSetHour, ColumnMetadata.named("SUN_SET_HOUR").withIndex(8).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(sunSetMinute, ColumnMetadata.named("SUN_SET_MINUTE").withIndex(9).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(sunSetAbsPosHor, ColumnMetadata.named("SUN_SET_ABS_POS_HOR").withIndex(10).ofType(Types.NUMERIC).notNull());
        addMetadata(sunSetAbsPosVert, ColumnMetadata.named("SUN_SET_ABS_POS_VERT").withIndex(11).ofType(Types.NUMERIC).notNull());
    }

}

