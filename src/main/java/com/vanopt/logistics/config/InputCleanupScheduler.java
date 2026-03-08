package com.vanopt.logistics.config;

import com.vanopt.logistics.service.OptimizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InputCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(InputCleanupScheduler.class);

    private final OptimizationService optimizationService;

    @Value("${vanopt.input-retention-days:30}")
    private int retentionDays;

    public InputCleanupScheduler(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @Scheduled(cron = "${vanopt.cleanup-cron:0 0 2 * * ?}")
    public void cleanupOldInput() {
        log.info("Cleaning up optimization input data older than {} days", retentionDays);
        optimizationService.deleteInputOlderThan(retentionDays);
    }
}
