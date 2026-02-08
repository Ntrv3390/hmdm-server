package com.hmdm.plugins.worktime.sync;

import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.rest.json.SyncApplicationInt;
import com.hmdm.rest.json.SyncResponseInt;
import com.hmdm.rest.json.SyncResponseHook;
import com.hmdm.plugins.worktime.service.WorkTimeService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class WorkTimeSyncResponseHook implements SyncResponseHook {

    private final UnsecureDAO unsecureDAO;
    private final WorkTimeService workTimeService;

    @Inject
    public WorkTimeSyncResponseHook(UnsecureDAO unsecureDAO, WorkTimeService workTimeService) {
        this.unsecureDAO = unsecureDAO;
        this.workTimeService = workTimeService;
    }

    @Override
    public SyncResponseInt handle(int deviceId, SyncResponseInt original) {
        try {
            // Resolve device to get customerId
            com.hmdm.persistence.domain.Device device = this.unsecureDAO.getDeviceById(deviceId);
            if (device == null) return original;

            // Current user assignment is not directly available from device record.
            // Device does not have a userId field, and userDeviceGroupsAccess is a permission mapping
            // not a device ownership mapping. Return original response unchanged.
            return original;
        } catch (Exception e) {
            return original;
        }
    }
}
