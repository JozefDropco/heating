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
public class AppLog extends com.querydsl.sql.RelationalPathBase<AppLog> {

    private static final long serialVersionUID = -332312148;

    public static final AppLog TEMPERATURE_LOG = new AppLog("APP_LOG");

    public final NumberPath<Long> id = createNumber("id",Long.class);
    public final NumberPath<Long> seqId = createNumber("seqId",Long.class);
    public final StringPath logLevel = createString("logLevel");
    public final DateTimePath<Date> date = createDateTime("msgDate", Date.class);
    public final StringPath message = createString("message");

    public final com.querydsl.sql.PrimaryKey<AppLog> pk = createPrimaryKey(id);

    public AppLog(String variable) {
        super(AppLog.class, forVariable(variable), "HEATING", "APP_LOG");
        addMetadata();
    }

    public AppLog(String schema, String variable) {
        super(AppLog.class, forVariable(variable), schema, "APP_LOG");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.NUMERIC).withSize(50).withDigits(5).notNull());
        addMetadata(seqId, ColumnMetadata.named("SEQ_ID").withIndex(2).ofType(Types.NUMERIC).withSize(50).withDigits(5).notNull());
        addMetadata(logLevel, ColumnMetadata.named("LOG_LEVEL").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(date, ColumnMetadata.named("MSG_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(message, ColumnMetadata.named("MESSAGE").withIndex(5).ofType(Types.VARCHAR).withSize(50).withDigits(5).notNull());
    }

}

