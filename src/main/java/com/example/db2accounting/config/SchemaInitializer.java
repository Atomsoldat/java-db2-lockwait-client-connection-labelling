package com.example.db2accounting.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("LOCK_TEST")) {
            jdbcTemplate.execute(
                "CREATE TABLE LOCK_TEST (id INT NOT NULL PRIMARY KEY, value VARCHAR(100))");
            jdbcTemplate.execute(
                "INSERT INTO LOCK_TEST (id, value) VALUES (1, 'test')");
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM SYSCAT.TABLES WHERE TABNAME = ?",
            Integer.class,
            tableName);
        return count != null && count > 0;
    }
}
