package com.xxl.job.admin.business.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AbstractMapperIntegrationTest.ScriptInitializationConfig.class)
class MysqlMapperIntegrationTest extends AbstractMapperIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("xxl_job")
            .withUsername("root")
            .withPassword("root_pwd");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations",
                () -> schemaLocation("doc/db/tables_xxl_job.sql"));
    }

    @Test
    void preservesMysqlMapperBehavior() throws Exception {
        assertDatabaseId("mysql");
        assertRegistryUpsertAndDeadlineQueries();
        assertLogReportUpsert();
        assertPaginationAndGeneratedKeys();
        assertLogCleanupAndAlarmQueries();
        assertLogGlueCleanup();
        assertScheduleLockInTransaction();
    }
}
