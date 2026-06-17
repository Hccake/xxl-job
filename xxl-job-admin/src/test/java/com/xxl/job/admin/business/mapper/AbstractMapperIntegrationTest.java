package com.xxl.job.admin.business.mapper;

import com.xxl.job.admin.business.model.XxlJobGroup;
import com.xxl.job.admin.business.model.XxlJobInfo;
import com.xxl.job.admin.business.model.XxlJobLog;
import com.xxl.job.admin.business.model.XxlJobLogGlue;
import com.xxl.job.admin.business.model.XxlJobLogReport;
import com.xxl.job.admin.business.model.XxlJobRegistry;
import com.xxl.job.admin.business.scheduler.config.XxlJobAdminBootstrap;
import com.xxl.job.admin.framework.mapper.XxlJobUserMapper;
import com.xxl.job.admin.framework.model.XxlJobUser;
import com.xxl.tool.core.DateTool;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.ApplicationDataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.autoconfigure.init.SqlInitializationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractMapperIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SqlInitializationProperties.class)
    static final class ScriptInitializationConfig {

        @Bean
        ApplicationDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
                SqlInitializationProperties properties) {
            return new ApplicationDataSourceScriptDatabaseInitializer(dataSource, properties) {

                @Override
                protected void customize(ResourceDatabasePopulator populator) {
                    populator.setCommentPrefixes("--", "#");
                }
            };
        }
    }

    @MockitoBean
    protected XxlJobAdminBootstrap xxlJobAdminBootstrap;

    @Resource
    protected SqlSessionFactory sqlSessionFactory;

    @Resource
    protected XxlJobRegistryMapper xxlJobRegistryMapper;

    @Resource
    protected XxlJobLogReportMapper xxlJobLogReportMapper;

    @Resource
    protected XxlJobGroupMapper xxlJobGroupMapper;

    @Resource
    protected XxlJobInfoMapper xxlJobInfoMapper;

    @Resource
    protected XxlJobLogMapper xxlJobLogMapper;

    @Resource
    protected XxlJobLogGlueMapper xxlJobLogGlueMapper;

    @Resource
    protected XxlJobLockMapper xxlJobLockMapper;

    @Resource
    protected XxlJobUserMapper xxlJobUserMapper;

    @Resource
    protected PlatformTransactionManager transactionManager;

    @Resource
    protected JdbcTemplate jdbcTemplate;

    protected static String schemaLocation(String relativePath) {
        Path moduleRelative = Path.of("..", relativePath).toAbsolutePath().normalize();
        if (Files.exists(moduleRelative)) {
            return moduleRelative.toUri().toString();
        }
        return Path.of(relativePath).toAbsolutePath().normalize().toUri().toString();
    }

    protected void assertDatabaseId(String expected) {
        assertEquals(expected, sqlSessionFactory.getConfiguration().getDatabaseId());
    }

    protected void assertRegistryUpsertAndDeadlineQueries() {
        Date oldTime = DateTool.addSeconds(new Date(), -120);
        Date newTime = new Date();
        String key = "test-registry-" + System.nanoTime();
        String staleKey = key + "-stale";

        assertTrue(xxlJobRegistryMapper.registrySaveOrUpdate("EXECUTOR", staleKey, "http://127.0.0.1:9998", oldTime) > 0);
        assertTrue(xxlJobRegistryMapper.registrySaveOrUpdate("EXECUTOR", key, "http://127.0.0.1:9999", oldTime) > 0);
        assertTrue(xxlJobRegistryMapper.registrySaveOrUpdate("EXECUTOR", key, "http://127.0.0.1:9999", newTime) > 0);

        Date deadline = DateTool.addSeconds(new Date(), -60);
        List<XxlJobRegistry> aliveRecords = xxlJobRegistryMapper.findAll(deadline).stream()
                .filter(item -> key.equals(item.getRegistryKey()))
                .toList();
        assertEquals(1, aliveRecords.size());

        Integer staleId = jdbcTemplate.queryForObject("select id from xxl_job_registry where registry_key = ?",
                Integer.class, staleKey);
        assertNotNull(staleId);
        List<Integer> deadIds = xxlJobRegistryMapper.findDead(deadline);
        assertTrue(deadIds.contains(staleId));
        assertFalse(deadIds.contains(aliveRecords.get(0).getId()));
    }

    protected void assertCaseInsensitiveBusinessKeysAndSearch() {
        assertRegistryKeysAreCaseInsensitive();
        assertUserNamesAreCaseInsensitive();
        assertSearchFiltersAreCaseInsensitive();
    }

    private void assertRegistryKeysAreCaseInsensitive() {
        Date oldTime = DateTool.addSeconds(new Date(), -120);
        Date newTime = new Date();
        String key = "test-case-registry-" + System.nanoTime();
        String registryValue = "HTTP://127.0.0.1:9997/CasePath";

        assertTrue(xxlJobRegistryMapper.registrySaveOrUpdate("EXECUTOR", key, registryValue, oldTime) > 0);
        assertTrue(xxlJobRegistryMapper.registrySaveOrUpdate("executor", key.toUpperCase(Locale.ROOT),
                registryValue.toLowerCase(Locale.ROOT), newTime) > 0);

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from xxl_job_registry where lower(registry_key) = lower(?)",
                Integer.class, key);
        assertEquals(1, count);

        Date deadline = DateTool.addSeconds(new Date(), -60);
        assertEquals(1, xxlJobRegistryMapper.findAll(deadline).stream()
                .filter(item -> key.equalsIgnoreCase(item.getRegistryKey()))
                .count());
    }

    private void assertUserNamesAreCaseInsensitive() {
        String username = "CaseUser" + System.nanoTime();
        XxlJobUser user = new XxlJobUser();
        user.setUsername(username);
        user.setPassword("password");
        user.setRole(0);
        user.setPermission("");
        assertEquals(1, xxlJobUserMapper.save(user));
        assertTrue(user.getId() > 0);

        assertNotNull(xxlJobUserMapper.loadByUserName(username.toLowerCase(Locale.ROOT)));
        assertFalse(xxlJobUserMapper.pageList(0, 10, username.toLowerCase(Locale.ROOT), -1).isEmpty());
        assertTrue(xxlJobUserMapper.pageListCount(0, 10, username.toLowerCase(Locale.ROOT), -1) > 0);

        XxlJobUser duplicate = new XxlJobUser();
        duplicate.setUsername(username.toUpperCase(Locale.ROOT));
        duplicate.setPassword("password");
        duplicate.setRole(0);
        duplicate.setPermission("");
        assertThrows(DataAccessException.class, () -> xxlJobUserMapper.save(duplicate));
    }

    private void assertSearchFiltersAreCaseInsensitive() {
        XxlJobGroup group = new XxlJobGroup();
        group.setAppname("CaseSearchApp" + System.nanoTime());
        group.setTitle("CaseSearchTitle");
        group.setAddressType(0);
        group.setAddressList("http://127.0.0.1:9999");
        group.setUpdateTime(new Date());
        assertEquals(1, xxlJobGroupMapper.save(group));

        assertFalse(xxlJobGroupMapper.pageList(0, 10,
                group.getAppname().toLowerCase(Locale.ROOT), null).isEmpty());
        assertTrue(xxlJobGroupMapper.pageListCount(0, 10,
                group.getAppname().toLowerCase(Locale.ROOT), null) > 0);
        assertFalse(xxlJobGroupMapper.pageList(0, 10,
                null, group.getTitle().toLowerCase(Locale.ROOT)).isEmpty());
        assertTrue(xxlJobGroupMapper.pageListCount(0, 10,
                null, group.getTitle().toLowerCase(Locale.ROOT)) > 0);

        XxlJobInfo info = new XxlJobInfo();
        info.setJobGroup(group.getId());
        info.setJobDesc("CaseSearchJob");
        info.setAddTime(new Date());
        info.setUpdateTime(new Date());
        info.setAuthor("CaseSearchAuthor");
        info.setAlarmEmail("");
        info.setScheduleType("NONE");
        info.setScheduleConf("");
        info.setMisfireStrategy("DO_NOTHING");
        info.setExecutorRouteStrategy("FIRST");
        info.setExecutorHandler("CaseSearchHandler");
        info.setExecutorParam("");
        info.setExecutorBlockStrategy("SERIAL_EXECUTION");
        info.setGlueType("BEAN");
        info.setGlueSource("");
        info.setGlueRemark("test");
        info.setGlueUpdatetime(new Date());
        info.setChildJobId("");
        assertEquals(1, xxlJobInfoMapper.save(info));

        assertFalse(xxlJobInfoMapper.pageList(0, 10, group.getId(), -1,
                info.getJobDesc().toLowerCase(Locale.ROOT), null, null).isEmpty());
        assertTrue(xxlJobInfoMapper.pageListCount(0, 10, group.getId(), -1,
                info.getJobDesc().toLowerCase(Locale.ROOT), null, null) > 0);
        assertFalse(xxlJobInfoMapper.pageList(0, 10, group.getId(), -1,
                null, info.getExecutorHandler().toLowerCase(Locale.ROOT), null).isEmpty());
        assertTrue(xxlJobInfoMapper.pageListCount(0, 10, group.getId(), -1,
                null, info.getExecutorHandler().toLowerCase(Locale.ROOT), null) > 0);
        assertFalse(xxlJobInfoMapper.pageList(0, 10, group.getId(), -1,
                null, null, info.getAuthor().toLowerCase(Locale.ROOT)).isEmpty());
        assertTrue(xxlJobInfoMapper.pageListCount(0, 10, group.getId(), -1,
                null, null, info.getAuthor().toLowerCase(Locale.ROOT)) > 0);
    }

    protected void assertLogReportUpsert() {
        Date triggerDay = DateTool.parseDateTime("2035-06-17 00:00:00");
        XxlJobLogReport report = new XxlJobLogReport();
        report.setTriggerDay(triggerDay);
        report.setRunningCount(1);
        report.setSucCount(2);
        report.setFailCount(3);
        report.setUpdateTime(new Date());

        assertTrue(xxlJobLogReportMapper.saveOrUpdate(report) > 0);
        report.setRunningCount(4);
        report.setSucCount(5);
        report.setFailCount(6);
        report.setUpdateTime(new Date());
        assertTrue(xxlJobLogReportMapper.saveOrUpdate(report) > 0);

        List<XxlJobLogReport> reports = xxlJobLogReportMapper.queryLogReport(triggerDay, triggerDay);
        assertEquals(1, reports.size());
        assertEquals(4, reports.get(0).getRunningCount());
        assertEquals(5, reports.get(0).getSucCount());
        assertEquals(6, reports.get(0).getFailCount());
    }

    protected void assertPaginationAndGeneratedKeys() {
        XxlJobGroup group = new XxlJobGroup();
        group.setAppname("test-app-" + System.nanoTime());
        group.setTitle("test-title");
        group.setAddressType(0);
        group.setAddressList("http://127.0.0.1:9999");
        group.setUpdateTime(new Date());
        assertEquals(1, xxlJobGroupMapper.save(group));
        assertTrue(group.getId() > 0);
        assertFalse(xxlJobGroupMapper.pageList(0, 10, "test-app", null).isEmpty());

        XxlJobInfo info = new XxlJobInfo();
        info.setJobGroup(group.getId());
        info.setJobDesc("test-job");
        info.setAddTime(new Date());
        info.setUpdateTime(new Date());
        info.setAuthor("tester");
        info.setAlarmEmail("");
        info.setScheduleType("NONE");
        info.setScheduleConf("");
        info.setMisfireStrategy("DO_NOTHING");
        info.setExecutorRouteStrategy("FIRST");
        info.setExecutorHandler("demoJobHandler");
        info.setExecutorParam("");
        info.setExecutorBlockStrategy("SERIAL_EXECUTION");
        info.setGlueType("BEAN");
        info.setGlueSource("");
        info.setGlueRemark("test");
        info.setGlueUpdatetime(new Date());
        info.setChildJobId("");
        assertEquals(1, xxlJobInfoMapper.save(info));
        assertTrue(info.getId() > 0);
        assertFalse(xxlJobInfoMapper.pageList(0, 10, group.getId(), -1, "test-job", null, null).isEmpty());

        XxlJobLog log = new XxlJobLog();
        log.setJobGroup(group.getId());
        log.setJobId(info.getId());
        log.setTriggerTime(new Date());
        log.setTriggerCode(200);
        log.setHandleCode(0);
        assertEquals(1, xxlJobLogMapper.save(log));
        assertTrue(log.getId() > 0);
        assertFalse(xxlJobLogMapper.pageList(0, 10, group.getId(), info.getId(), null, null, 3).isEmpty());

        XxlJobUser user = new XxlJobUser();
        user.setUsername("test-user-" + System.nanoTime());
        user.setPassword("password");
        user.setRole(0);
        user.setPermission(String.valueOf(group.getId()));
        assertEquals(1, xxlJobUserMapper.save(user));
        assertTrue(user.getId() > 0);
        assertFalse(xxlJobUserMapper.pageList(0, 10, user.getUsername(), -1).isEmpty());
    }

    protected void assertLogCleanupAndAlarmQueries() {
        saveLog(DateTool.addDays(new Date(), -2), 200, 200);
        long failedId = saveLog(DateTool.addDays(new Date(), -1), 500, 0);

        List<Long> clearIds = xxlJobLogMapper.findClearLogIds(1, 1, new Date(), 1, 10);
        assertFalse(clearIds.isEmpty());
        assertTrue(xxlJobLogMapper.findFailJobLogIds(10).contains(failedId));

        Date reportFrom = DateTool.parseDateTime("2035-06-18 00:00:00");
        saveLog(DateTool.addSeconds(reportFrom, 1), 200, 0);
        saveLog(DateTool.addSeconds(reportFrom, 2), 200, 200);
        saveLog(DateTool.addSeconds(reportFrom, 3), 500, 0);

        Map<String, Object> report = xxlJobLogMapper.findLogReport(reportFrom, DateTool.addSeconds(reportFrom, 10));
        assertEquals(3, intValue(report.get("triggerDayCount")));
        assertEquals(1, intValue(report.get("triggerDayCountRunning")));
        assertEquals(1, intValue(report.get("triggerDayCountSuc")));
    }

    protected void assertLogGlueCleanup() {
        int jobId = 1;
        for (int i = 0; i < 3; i++) {
            XxlJobLogGlue glue = new XxlJobLogGlue();
            glue.setJobId(jobId);
            glue.setGlueType("BEAN");
            glue.setGlueSource("source-" + i);
            glue.setGlueRemark("remark-" + i);
            glue.setAddTime(DateTool.addSeconds(new Date(), i));
            glue.setUpdateTime(DateTool.addSeconds(new Date(), i));
            assertEquals(1, xxlJobLogGlueMapper.save(glue));
            assertTrue(glue.getId() > 0);
        }

        assertTrue(xxlJobLogGlueMapper.removeOld(jobId, 1) >= 0);
        assertEquals(1, xxlJobLogGlueMapper.findByJobId(jobId).size());
    }

    protected void assertScheduleLockInTransaction() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch competingTransactionStarted = new CountDownLatch(1);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        boolean committed = false;
        try {
            assertEquals("schedule_lock", xxlJobLockMapper.scheduleLock());
            Future<String> competingLock = executor.submit(() -> {
                TransactionStatus competingStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
                boolean competingCommitted = false;
                try {
                    competingTransactionStarted.countDown();
                    String lockedRecord = xxlJobLockMapper.scheduleLock();
                    transactionManager.commit(competingStatus);
                    competingCommitted = true;
                    return lockedRecord;
                } finally {
                    if (!competingCommitted && !competingStatus.isCompleted()) {
                        transactionManager.rollback(competingStatus);
                    }
                }
            });

            assertTrue(competingTransactionStarted.await(2, TimeUnit.SECONDS));
            TimeUnit.MILLISECONDS.sleep(300);
            assertFalse(competingLock.isDone(),
                    "Competing schedule lock query completed before the first transaction released the row lock");

            transactionManager.commit(status);
            committed = true;
            assertEquals("schedule_lock", competingLock.get(5, TimeUnit.SECONDS));
        } finally {
            if (!committed && !status.isCompleted()) {
                transactionManager.rollback(status);
            }
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    private int intValue(Object value) {
        assertNotNull(value);
        return Integer.parseInt(String.valueOf(value));
    }

    private long saveLog(Date triggerTime, int triggerCode, int handleCode) {
        XxlJobLog log = new XxlJobLog();
        log.setJobGroup(1);
        log.setJobId(1);
        log.setTriggerTime(triggerTime);
        log.setTriggerCode(triggerCode);
        log.setHandleCode(handleCode);
        assertEquals(1, xxlJobLogMapper.save(log));
        return log.getId();
    }
}
