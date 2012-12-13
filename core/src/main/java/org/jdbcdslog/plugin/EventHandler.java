package org.jdbcdslog.plugin;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

public interface EventHandler {
    /**
     * A prepared statement is successfully executed.
     * @param stmt
     * @param sql the sql executed
     * @param parameters the parameters bound to the executed sql
     * @param time time spent for executing the statement
     */
    void statement(Statement stmt, String sql, Map parameters, long time);
}
