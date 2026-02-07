package com.hmdm.plugins.worktime.persistence;

import com.hmdm.plugins.worktime.model.GlobalWorkTimePolicy;

public interface WorkTimeDAO {

    GlobalWorkTimePolicy getGlobalPolicy(int customerId);

    void saveGlobalPolicy(GlobalWorkTimePolicy policy);
}
