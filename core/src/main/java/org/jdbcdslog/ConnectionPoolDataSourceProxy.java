package org.jdbcdslog;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ConnectionPoolDataSourceProxy extends DataSourceProxyBase implements DataSource, ConnectionPoolDataSource {

    private static final long serialVersionUID = 5094791657099299920L;

    public ConnectionPoolDataSourceProxy() throws JDBCDSLogException {
        super();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public Object unwrap(Class iface) throws SQLException {
        return super.unwrap(iface);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
