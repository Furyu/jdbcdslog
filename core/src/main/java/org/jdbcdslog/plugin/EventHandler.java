package org.jdbcdslog.plugin;

import java.util.Map;

public interface EventHandler {
    /**
     * A prepared statement is successfully executed.
     * @param sql the sql executed
     * @param parameters the parameters bound to the executed sql
     * @param time time spent for executing the statement
     */
    void preparedStatement(String sql, Map parameters, long time);
}
