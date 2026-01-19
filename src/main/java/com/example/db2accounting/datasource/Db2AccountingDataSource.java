package com.example.db2accounting.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLClientInfoException;

import javax.sql.DataSource;

/**
 * A DataSource wrapper that sets DB2 CLIENT_ACCTING and CLIENT_APPLNAME
 * special registers on each connection request.
 */
public class Db2AccountingDataSource implements DataSource {

    private static final String BASE_PACKAGE = Db2AccountingDataSource.class.getPackageName().replace(".datasource", "");
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private static final String CLIENT_USER = "Albus Dumbledore";
    private static final String CLIENT_HOSTNAME = resolveHostname();

    private static String resolveHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            return "unknown";
        }
    }

    private final DataSource delegate;
    private volatile boolean enabled;

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
        try {
            connection.setClientInfo("ApplicationName", BASE_PACKAGE);
            connection.setClientInfo("ClientAccountingInformation", caller);
            connection.setClientInfo("ClientUser", CLIENT_USER);
            connection.setClientInfo("ClientHostname", CLIENT_HOSTNAME);
            System.out.println("Set client info: ApplicationName=" + BASE_PACKAGE + ", ClientAccountingInformation=" + caller + ", ClientUser=" + CLIENT_USER + ", ClientHostname=" + CLIENT_HOSTNAME);
        } catch (SQLClientInfoException e) {
            System.err.println("Failed to set client info: " + e.getMessage());
            System.err.println("Failed properties: " + e.getFailedProperties());
        }
    }

    private String findCaller() {
        return STACK_WALKER.walk(frames -> frames
                .filter(f -> f.getClassName().startsWith(BASE_PACKAGE)
                          && !f.getClassName().contains(".datasource."))
                .findFirst()
                .map(f -> {
                    String relativePath = f.getClassName().substring(BASE_PACKAGE.length() + 1);
                    return relativePath + "." + f.getMethodName() + ":" + f.getLineNumber();
                })
                .orElse("Unknown"));
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
