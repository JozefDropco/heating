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
public class DoubleSetting extends com.querydsl.sql.RelationalPathBase<DoubleSetting> {

    private static final long serialVersionUID = -332312148;

    public static final DoubleSetting DOUBLE = new DoubleSetting("DOUBLE_SETTING");

    public final StringPath refCd = createString("refCd");

    public final NumberPath<Double> value = createNumber("value",Double.class);

    public final DateTimePath<Date> modifiedTs = createDateTime("modifiedTs", Date.class);

    public final StringPath description = createString("description");
    public final StringPath group = createString("group");
    public final StringPath valueType = createString("valueType");

    public final com.querydsl.sql.PrimaryKey<DoubleSetting> tV21RefRegionPk = createPrimaryKey(refCd);

    public DoubleSetting(String variable) {
        super(DoubleSetting.class, forVariable(variable), "HEATING", "DOUBLE_SETTING");
        addMetadata();
    }

    public DoubleSetting(String schema, String variable) {
        super(DoubleSetting.class, forVariable(variable), schema, "DOUBLE_SETTING");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(refCd, ColumnMetadata.named("REF_CD").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(5).notNull());
        addMetadata(modifiedTs, ColumnMetadata.named("MODIFIED_TS").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(group, ColumnMetadata.named("GROUP").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(valueType, ColumnMetadata.named("VALUE_TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

