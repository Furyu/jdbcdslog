package org.jdbcdslog.plugin;

import java.sql.Statement;
import java.util.Map;

public class EventHandlerAPI {
    private static EventHandler instance;

    public static void statement(Statement stmt, Map parameters, long time, String sql) {
        getInstance().statement(stmt, sql, parameters, time);
    }

    @Deprecated
    public static EventHandler getEventHandler() {
        return getInstance();
    }

    public static EventHandler getInstance() {
        if (instance == null) {
            instance = EventHandlerFactory.createFromConfig();
        }
        return instance;
    }

    public static void setInstance(EventHandler arg) {
        instance = arg;
    }
}
