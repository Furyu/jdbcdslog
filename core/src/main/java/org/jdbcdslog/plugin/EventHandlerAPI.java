package org.jdbcdslog.plugin;

import java.sql.PreparedStatement;
import java.util.Map;

public class EventHandlerAPI {
    private static EventHandler plugged = EventHandlerFactory.createFromConfig();

    public static void preparedStatement(PreparedStatement stmt, Map parameters, long time, String sql) {
        plugged.preparedStatement(stmt, sql, parameters, time);
    }

    public static EventHandler getEventHandler() {
        return plugged;
    }
}
