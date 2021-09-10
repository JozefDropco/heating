package org.dropco.smarthome.database;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class Db {
    private static DataSource dataSource;

    static {
        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
        ds.setURL("jdbc:mysql://localhost/heating?"
                + "user=heating&password=h1e2a3t4i5n6g7&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Vienna&characterEncoding=UTF-8");
        dataSource = ds;
    }

    public static <D extends Dao> void acceptDao(D dao, Consumer<D> toExec) {
        try (Connection conn =  dataSource.getConnection()) {
            dao.setConnection(conn);
            toExec.accept(dao);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get the connection",e);
        }
    }

    public static <R,D extends Dao> R applyDao(D dao, Function<D,R> toExec) {
        try (Connection conn = dataSource.getConnection()) {
            dao.setConnection(conn);
            return toExec.apply(dao);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get the connection",e);
        }
    }
}
