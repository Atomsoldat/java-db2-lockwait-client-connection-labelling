package com.example.db2accounting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * Demo service to test DB2 client accounting info.
 * Each method that uses the database will have its class.method passed to DB2.
 */
@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DemoService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Retrieves current DB2 client info from special registers.
     * Useful for verifying that the accounting info is being set correctly.
     */
    public Map<String, String> getCurrentClientInfo() {
        log.info("Fetching current DB2 client info");

        Map<String, String> clientInfo = new HashMap<>();

        jdbcTemplate.query(
            "SELECT CURRENT CLIENT_APPLNAME AS APPLNAME, " +
            "CURRENT CLIENT_ACCTNG AS ACCTNG, " +
            "CURRENT CLIENT_WRKSTNNAME AS WRKSTNNAME, " +
            "CURRENT CLIENT_USERID AS USERID " +
            "FROM SYSIBM.SYSDUMMY1",
            rs -> {
                clientInfo.put("CLIENT_APPLNAME", rs.getString("APPLNAME"));
                clientInfo.put("CLIENT_ACCTNG", rs.getString("ACCTNG"));
                clientInfo.put("CLIENT_WRKSTNNAME", rs.getString("WRKSTNNAME"));
                clientInfo.put("CLIENT_USERID", rs.getString("USERID"));
            }
        );

        return clientInfo;
    }

    /**
     * Example business method - fetching some data.
     * The client info should show "DemoService.fetchSomeData"
     */
    public String fetchSomeData() {
        log.info("Executing fetchSomeData");

        return jdbcTemplate.queryForObject(
            "SELECT 'Hello from DB2' FROM SYSIBM.SYSDUMMY1",
            String.class
        );
    }

    /**
     * Another example method to show different accounting info.
     */
    public int countRecords(String tableName) {
        log.info("Counting records in table: {}", tableName);

        // Note: In production, use parameterized queries to prevent SQL injection
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM SYSIBM.SYSDUMMY1",
            Integer.class
        );
    }

    /**
     * Simulates a performance test by executing multiple queries.
     */
    public long performanceTest(int iterations) {
        log.info("Running performance test with {} iterations", iterations);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            jdbcTemplate.queryForObject(
                "SELECT 1 FROM SYSIBM.SYSDUMMY1",
                Integer.class
            );
        }

        long duration = System.currentTimeMillis() - startTime;
        double avg_latency = (double) duration / iterations * 1000;
        String message = String.format("Performance test completed in %d ms; average latency: %.2f us", duration, avg_latency);
        log.info(message);

        return duration;
    }

    /**
     * Holds a lock on LOCK_TEST table for the specified duration.
     * Call this endpoint twice simultaneously to create a lock wait.
     */
    public void holdLock(int holdSeconds) throws Exception {
        log.info("Acquiring lock and holding for {} seconds", holdSeconds);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE LOCK_TEST SET value = ? WHERE id = 1")) {
                stmt.setString(1, "locked-" + System.currentTimeMillis());
                stmt.executeUpdate();

                log.info("Lock acquired, sleeping for {} seconds", holdSeconds);
                Thread.sleep(holdSeconds * 1000L);

                conn.commit();
                log.info("Lock released");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
