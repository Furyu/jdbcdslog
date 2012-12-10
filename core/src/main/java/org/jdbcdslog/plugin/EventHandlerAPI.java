package org.jdbcdslog.plugin;

import java.util.Map;

public class EventHandlerAPI {
    private static EventHandler plugged = EventHandlerFactory.createFromConfig();

    public static void preparedStatement(String sql, Map parameters, long time) {
        plugged.preparedStatement(sql, parameters, time);
    }

    public static EventHandler getEventHandler() {
        return plugged;
    }
}
