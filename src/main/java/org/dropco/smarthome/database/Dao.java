package org.dropco.smarthome.database;

import java.sql.Connection;

public interface Dao {

    void setConnection(Connection connection);
}
