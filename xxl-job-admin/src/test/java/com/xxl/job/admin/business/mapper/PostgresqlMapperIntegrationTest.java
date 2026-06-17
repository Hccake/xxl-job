package com.xxl.job.admin.business.mapper;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostgresqlMapperIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("xxl_job")
            .withUsername("xxl_job")
            .withPassword("xxl_job");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", PostgresqlMapperIntegrationTest::postgresqlSchemaLocation);
    }

    private static String postgresqlSchemaLocation() {
        Path moduleRelative = Path.of("..", "doc", "db", "tables_xxl_job_postgresql.sql")
                .toAbsolutePath()
                .normalize();
        if (Files.exists(moduleRelative)) {
            return moduleRelative.toUri().toString();
        }
        return Path.of("doc", "db", "tables_xxl_job_postgresql.sql")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
    }

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void resolvesPostgresqlDatabaseId() {
        assertEquals("postgresql", sqlSessionFactory.getConfiguration().getDatabaseId());
    }
}
