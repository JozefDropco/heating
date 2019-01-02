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
public class SolarPosition extends com.querydsl.sql.RelationalPathBase<SolarPosition> {

    private static final long serialVersionUID = -332312148;

    public static final SolarPosition SOLAR_POSITION = new SolarPosition("SOLAR_POSITION");

    public final NumberPath<Long> id = createNumber("id",Long.class);
    public final NumberPath<Integer> horizontalPosition = createNumber("horizontal",Integer.class);
    public final NumberPath<Integer> verticalPosition = createNumber("vertical",Integer.class);

    public final com.querydsl.sql.PrimaryKey<SolarPosition> posPk = createPrimaryKey(id);

    public SolarPosition(String variable) {
        super(SolarPosition.class, forVariable(variable), "HEATING", "SOLAR_POSITION");
        addMetadata();
    }

    public SolarPosition(String schema, String variable) {
        super(SolarPosition.class, forVariable(variable), schema, "SOLAR_POSITION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(horizontalPosition, ColumnMetadata.named("HORIZONTAL").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(0));
        addMetadata(verticalPosition, ColumnMetadata.named("VERTICAL").withIndex(3).ofType(Types.NUMERIC).withSize(50).withDigits(0));
    }

}

