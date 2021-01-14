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
public class LongSetting extends com.querydsl.sql.RelationalPathBase<LongSetting> {

    private static final long serialVersionUID = -332312148;

    public static final LongSetting LONG = new LongSetting("LONG_SETTING");

    public final StringPath refCd = createString("refCd");

    public final NumberPath<Long> value = createNumber("value",Long.class);

    public final DateTimePath<Date> modifiedTs = createDateTime("modifiedTs", Date.class);

    public final StringPath description = createString("description");
    public final StringPath group = createString("group");
    public final StringPath valueType = createString("valueType");

    public final com.querydsl.sql.PrimaryKey<LongSetting> tV21RefRegionPk = createPrimaryKey(refCd);

    public LongSetting(String variable) {
        super(LongSetting.class, forVariable(variable), "HEATING", "LONG_SETTING");
        addMetadata();
    }

    public LongSetting(String schema, String variable) {
        super(LongSetting.class, forVariable(variable), schema, "LONG_SETTING");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(refCd, ColumnMetadata.named("REF_CD").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(0).notNull());
        addMetadata(modifiedTs, ColumnMetadata.named("MODIFIED_TS").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(group, ColumnMetadata.named("GROUP").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(valueType, ColumnMetadata.named("VALUE_TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

