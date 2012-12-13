package org.jdbcdslog.plugin;

import java.sql.PreparedStatement;
import java.util.Map;

public class StdoutEventHandler implements EventHandler {
    @Override
    public void preparedStatement(PreparedStatement stmt, String sql, Map parameters, long time) {
        System.out.println("sql = " + sql + ", parameters=" + parameters + ", time=" + time);
    }
}
