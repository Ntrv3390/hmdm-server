package com.hmdm.plugins.worktime.task;

import com.hmdm.plugins.worktime.model.WorkTimeDeviceOverride;
import com.hmdm.plugins.worktime.persistence.WorkTimeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Task to clean up expired device exceptions.
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
     * Clean up all expired device exceptions across all customers.
     * This method is called periodically by the BackgroundTaskRunnerService.
     */
    public void cleanupExpiredExceptions() {
        try {
            log.debug("Running expired exception cleanup task");
            LocalDateTime now = LocalDateTime.now();
            
            // Get all device overrides
            List<WorkTimeDeviceOverride> allOverrides = workTimeDAO.getAllDeviceOverrides();
            
            int cleanedCount = 0;
            for (WorkTimeDeviceOverride override : allOverrides) {
                if (isExpired(override, now)) {
                    log.info("Deleting expired exception for device {} in customer {}", 
                             override.getDeviceId(), override.getCustomerId());
                    workTimeDAO.deleteDeviceOverride(override.getCustomerId(), override.getDeviceId());
                    cleanedCount++;
                }
            }
            
            if (cleanedCount > 0) {
                log.info("Cleaned up {} expired device exceptions", cleanedCount);
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
    private boolean isExpired(WorkTimeDeviceOverride override, LocalDateTime now) {
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
