package org.jdbcdslog.plugin;

import java.util.Map;

public class StdoutEventHandler implements EventHandler {
    @Override
    public void preparedStatement(String sql, Map parameters, long time) {
        System.out.println("sql = " + sql + ", parameters=" + parameters + ", time=" + time);
    }
}
