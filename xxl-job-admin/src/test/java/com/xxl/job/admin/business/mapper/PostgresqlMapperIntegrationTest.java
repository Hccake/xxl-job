package com.xxl.job.admin.business.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AbstractMapperIntegrationTest.ScriptInitializationConfig.class)
class PostgresqlMapperIntegrationTest extends AbstractMapperIntegrationTest {

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
        registry.add("spring.sql.init.schema-locations",
                () -> schemaLocation("doc/db/tables_xxl_job_postgresql.sql"));
    }

    @Test
    void supportsPostgresqlMapperBehavior() throws Exception {
        assertDatabaseId("postgresql");
        assertRegistryUpsertAndDeadlineQueries();
        assertLogReportUpsert();
        assertPaginationAndGeneratedKeys();
        assertLogCleanupAndAlarmQueries();
        assertLogGlueCleanup();
        assertScheduleLockInTransaction();
    }
}
