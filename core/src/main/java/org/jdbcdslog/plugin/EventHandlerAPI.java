package org.jdbcdslog.plugin;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

public class EventHandlerAPI {
    private static EventHandler plugged = EventHandlerFactory.createFromConfig();

    public static void statement(Statement stmt, Map parameters, long time, String sql) {
        plugged.statement(stmt, sql, parameters, time);
    }

    public static EventHandler getEventHandler() {
        return plugged;
    }
}
