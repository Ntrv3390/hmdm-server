package com.hmdm.plugins.worktime.persistence;

import com.hmdm.plugins.worktime.model.WorkTimePolicy;
import com.hmdm.plugins.worktime.model.WorkTimeUserOverride;

import java.util.List;

public interface WorkTimeDAO {

    WorkTimePolicy getGlobalPolicy(int customerId);

    void saveGlobalPolicy(WorkTimePolicy policy);

    // User override management (admin only)
    List<WorkTimeUserOverride> getUserOverrides(int customerId);

    WorkTimeUserOverride getUserOverride(int customerId, int userId);

    void saveUserOverride(WorkTimeUserOverride policy);

    void deleteUserOverride(int customerId, int userId);

    // Get all user overrides across all customers (for cleanup task)
    List<WorkTimeUserOverride> getAllUserOverrides();
}

