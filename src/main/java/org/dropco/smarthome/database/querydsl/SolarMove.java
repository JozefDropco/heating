package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * Region is a Querydsl query type for Region
 */
public class SolarMove extends com.querydsl.sql.RelationalPathBase<SolarMove> {

    private static final long serialVersionUID = -332312148;

    public static final SolarMove SOLAR_MOVE = new SolarMove("m");

    public final NumberPath<Integer> month = createNumber("month",Integer.class);
    public final NumberPath<Integer> hour = createNumber("hour",Integer.class);
    public final NumberPath<Integer> minute = createNumber("minute",Integer.class);
    public final NumberPath<Integer> horizontalSteps = createNumber("horizontal",Integer.class);
    public final NumberPath<Integer> verticalSteps = createNumber("vertical",Integer.class);

    public final com.querydsl.sql.PrimaryKey<SolarMove> posPk = createPrimaryKey(month,hour,minute);

    public SolarMove(String variable) {
        super(SolarMove.class, forVariable(variable), "HEATING", "SOLAR_MOVE");
        addMetadata();
    }

    public SolarMove(String schema, String variable) {
        super(SolarMove.class, forVariable(variable), schema, "SOLAR_MOVE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(month, ColumnMetadata.named("MONTH").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(hour, ColumnMetadata.named("HOUR").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(minute, ColumnMetadata.named("MINUTE").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(horizontalSteps, ColumnMetadata.named("HORIZONTAL").withIndex(4).ofType(Types.NUMERIC).withSize(50).withDigits(0));
        addMetadata(verticalSteps, ColumnMetadata.named("VERTICAL").withIndex(5).ofType(Types.NUMERIC).withSize(50).withDigits(0));
    }

}

