package org.dropco.smarthome.database.querydsl;

import com.querydsl.core.types.dsl.DateTimePath;
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
public class StringSetting extends com.querydsl.sql.RelationalPathBase<StringSetting> {

    private static final long serialVersionUID = -332312148;

    public static final StringSetting STRING = new StringSetting("STRING_SETTING");

    public final StringPath refCd = createString("refCd");

    public final StringPath value = createString("value");

    public final DateTimePath<Date> modifiedTs = createDateTime("modifiedTs", Date.class);


    public final com.querydsl.sql.PrimaryKey<StringSetting> tV21RefRegionPk = createPrimaryKey(refCd);

    public StringSetting(String variable) {
        super(StringSetting.class, forVariable(variable), "HEATING", "STRING_SETTING");
        addMetadata();
    }

    public StringSetting(String schema,String variable) {
        super(StringSetting.class, forVariable(variable), schema, "STRING_SETTING");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(refCd, ColumnMetadata.named("REF_CD").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modifiedTs, ColumnMetadata.named("MODIFIED_TS").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
    }

}

