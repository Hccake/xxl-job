package com.xxl.job.admin.business.model.dto;

import com.xxl.job.admin.business.model.XxlJobLog;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XxlJobLogDTOTest {

    @Test
    void shouldAllowLogWithoutHandleTime() {
        XxlJobLog log = new XxlJobLog();
        log.setTriggerTime(new Date());
        log.setHandleTime(null);

        XxlJobLogDTO dto = new XxlJobLogDTO(log);

        assertNotNull(dto.getTriggerTime());
        assertEquals("", dto.getHandleTime());
    }
}
