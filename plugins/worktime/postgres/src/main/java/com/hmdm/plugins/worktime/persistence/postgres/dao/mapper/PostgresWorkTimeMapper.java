package com.hmdm.plugins.worktime.persistence.postgres.dao.mapper;

import com.hmdm.plugins.worktime.model.GlobalWorkTimePolicy;
import org.apache.ibatis.annotations.Param;

public interface PostgresWorkTimeMapper {

    GlobalWorkTimePolicy getGlobalPolicy(@Param("customerId") int customerId);

    void insertGlobalPolicy(GlobalWorkTimePolicy policy);

    void updateGlobalPolicy(GlobalWorkTimePolicy policy);
}
