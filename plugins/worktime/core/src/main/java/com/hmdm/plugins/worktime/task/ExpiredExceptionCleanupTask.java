package com.hmdm.plugins.worktime.task;

import com.hmdm.plugins.worktime.model.WorkTimeUserOverride;
import com.hmdm.plugins.worktime.persistence.WorkTimeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Task to clean up expired user exceptions.
 * Runs periodically to check for and remove expired exceptions.
 */
@Singleton
public class ExpiredExceptionCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(ExpiredExceptionCleanupTask.class);
    
    private final WorkTimeDAO workTimeDAO;

    @Inject
    public ExpiredExceptionCleanupTask(WorkTimeDAO workTimeDAO) {
        this.workTimeDAO = workTimeDAO;
    }

    /**
     * Clean up all expired user exceptions across all customers.
     * This method is called periodically by the BackgroundTaskRunnerService.
     */
    public void cleanupExpiredExceptions() {
        try {
            log.debug("Running expired exception cleanup task");
            LocalDateTime now = LocalDateTime.now();
            
            // Get all user overrides
            List<WorkTimeUserOverride> allOverrides = workTimeDAO.getAllUserOverrides();
            
            int cleanedCount = 0;
            for (WorkTimeUserOverride override : allOverrides) {
                if (isExpired(override, now)) {
                    log.info("Deleting expired exception for user {} in customer {}", 
                             override.getUserId(), override.getCustomerId());
                    workTimeDAO.deleteUserOverride(override.getCustomerId(), override.getUserId());
                    cleanedCount++;
                }
            }
            
            if (cleanedCount > 0) {
                log.info("Cleaned up {} expired user exceptions", cleanedCount);
            } else {
                log.debug("No expired exceptions found");
            }
        } catch (Exception e) {
            log.error("Error during expired exception cleanup", e);
        }
    }

    /**
     * Check if an exception has expired.
     */
    private boolean isExpired(WorkTimeUserOverride override, LocalDateTime now) {
        // Only check exceptions (enabled=false with date range)
        if (override.isEnabled()) {
            return false;
        }
        
        if (override.getEndDateTime() == null) {
            return false;
        }
        
        LocalDateTime endTime = override.getEndDateTime().toLocalDateTime();
        return now.isAfter(endTime);
    }
}
