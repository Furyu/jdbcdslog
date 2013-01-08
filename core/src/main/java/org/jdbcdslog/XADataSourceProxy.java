package org.jdbcdslog;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class XADataSourceProxy extends DataSourceProxyBase implements XADataSource, DataSource {

    private static final long serialVersionUID = -2923593005281631348L;

    public XADataSourceProxy() throws JDBCDSLogException {
        super();
    }

    // Overrides on JDK7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public Object unwrap(Class iface) throws SQLException {
        return super.unwrap(iface);
    }
}
