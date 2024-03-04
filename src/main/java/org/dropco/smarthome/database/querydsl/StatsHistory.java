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
public class StatsHistory extends com.querydsl.sql.RelationalPathBase<StatsHistory> {

    public final StringPath name = createString("name");
    public final DateTimePath<Date> asOfDate = createDateTime("asOfDate", Date.class);
    public final NumberPath<Long>  count = createNumber("count",Long.class);
    public final NumberPath<Long>  secondsSum = createNumber("secondsSum",Long.class);


    public final com.querydsl.sql.PrimaryKey<StatsHistory> pk = createPrimaryKey(asOfDate,name);

    public StatsHistory(String variable) {
        super(StatsHistory.class, forVariable(variable), "HEATING", "STATS_HISTORY");
        addMetadata();
    }

    public StatsHistory(String schema, String variable) {
        super(StatsHistory.class, forVariable(variable), schema, "STATS_HISTORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(asOfDate, ColumnMetadata.named("AS_OF_DATE").withIndex(2).ofType(Types.DATE).withSize(11).withDigits(6).notNull());
        addMetadata(count, ColumnMetadata.named("TICK_COUNT").withIndex(3).ofType(Types.NUMERIC).withSize(11).withDigits(6).notNull());
        addMetadata(secondsSum, ColumnMetadata.named("SECONDS_SUM").withIndex(3).ofType(Types.NUMERIC).withSize(11).withDigits(6).notNull());
    }

}

