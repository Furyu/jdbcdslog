package org.jdbcdslog;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DataSourceProxy extends DataSourceProxyBase implements DataSource {

    private static final long serialVersionUID = -6888072076120346186L;

    public DataSourceProxy() throws JDBCDSLogException {
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
