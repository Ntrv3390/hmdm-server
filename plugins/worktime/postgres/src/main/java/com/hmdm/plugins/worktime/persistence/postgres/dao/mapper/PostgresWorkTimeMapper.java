package com.hmdm.plugins.worktime.persistence.postgres.dao.mapper;

import com.hmdm.plugins.worktime.model.WorkTimePolicy;
import com.hmdm.plugins.worktime.model.WorkTimeUserOverride;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PostgresWorkTimeMapper {

    WorkTimePolicy getGlobalPolicy(@Param("customerId") int customerId);

    void insertGlobalPolicy(WorkTimePolicy policy);

    void updateGlobalPolicy(WorkTimePolicy policy);

    // User overrides
    List<WorkTimeUserOverride> getUserOverrides(@Param("customerId") int customerId);

    WorkTimeUserOverride getUserOverride(@Param("customerId") int customerId, @Param("userId") int userId);

    void insertUserOverride(WorkTimeUserOverride override);

    void updateUserOverride(WorkTimeUserOverride override);

    void deleteUserOverride(@Param("customerId") int customerId, @Param("userId") int userId);

    // Get all user overrides across all customers (for cleanup task)
    List<WorkTimeUserOverride> getAllUserOverrides();
}
