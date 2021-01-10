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
public class Stats extends com.querydsl.sql.RelationalPathBase<Stats> {

    private static final long serialVersionUID = -33231548;

    public static final Stats DOUBLE = new Stats("STATS");

    public final NumberPath<Long> id = createNumber("id",Long.class);
    public final StringPath name = createString("name");


    public final DateTimePath<Date> fromDate = createDateTime("toDate", Date.class);
    public final DateTimePath<Date> toDate = createDateTime("fromDate", Date.class);


    public final com.querydsl.sql.PrimaryKey<Stats> pk = createPrimaryKey(id);

    public Stats(String variable) {
        super(Stats.class, forVariable(variable), "HEATING", "STATS");
        addMetadata();
    }

    public Stats(String schema, String variable) {
        super(Stats.class, forVariable(variable), schema, "STATS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(255).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(fromDate, ColumnMetadata.named("FROM_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(toDate, ColumnMetadata.named("TO_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
    }

}

