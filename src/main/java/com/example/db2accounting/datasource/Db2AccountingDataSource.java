package com.example.db2accounting.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A DataSource wrapper that sets DB2 CLIENT_ACCTING and CLIENT_APPLNAME
 * special registers on each connection request.
 */
public class Db2AccountingDataSource implements DataSource {

    private final DataSource delegate;
    private volatile boolean enabled;

    private static final StackWalker STACK_WALKER = StackWalker.getInstance();

    public Db2AccountingDataSource(DataSource delegate, boolean enabled) {
        this.delegate = delegate;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = delegate.getConnection();
        if (enabled) {
            setClientInfo(connection);
        }
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = delegate.getConnection(username, password);
        if (enabled) {
            setClientInfo(connection);
        }
        return connection;
    }

    // https://www.ibm.com/docs/en/db2/11.5.x?topic=jies-providing-extended-client-information-data-source-client-info-properties
    private void setClientInfo(Connection connection) throws SQLException {
        String caller = findCaller();
        connection.setClientInfo("ApplicationName", "Example Application");
        connection.setClientInfo("ClientAccountingInformation", caller);
        connection.setClientInfo("ClientUser", "Albus Dumbledore");
    }

    private String findCaller() {
        return STACK_WALKER.walk(frames -> frames
                .filter(f -> f.getClassName().startsWith("com.example.db2accounting")
                          && !f.getClassName().contains(".datasource."))
                .findFirst()
                .map(f -> extractSimpleName(f.getClassName()) + "." + f.getMethodName())
                .orElse("Unknown"));
    }

    private String extractSimpleName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot >= 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        try {
            return delegate.getParentLogger();
        } catch (Exception e) {
            return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || delegate.isWrapperFor(iface);
    }
}
