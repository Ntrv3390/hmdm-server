package com.hmdm.plugins.worktime.persistence.postgres.dao;

import javax.inject.Inject;

import com.hmdm.plugins.worktime.model.WorkTimePolicy;
import com.hmdm.plugins.worktime.model.WorkTimeUserOverride;
import com.hmdm.plugins.worktime.persistence.WorkTimeDAO;
import com.hmdm.plugins.worktime.persistence.postgres.dao.mapper.PostgresWorkTimeMapper;
import org.mybatis.guice.transactional.Transactional;
import java.util.List;

public class PostgresWorkTimeDAO implements WorkTimeDAO {

    private final PostgresWorkTimeMapper mapper;

    @Inject
    public PostgresWorkTimeDAO(PostgresWorkTimeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public WorkTimePolicy getGlobalPolicy(int customerId) {
        return mapper.getGlobalPolicy(customerId);
    }

    @Override
    @Transactional
    public void saveGlobalPolicy(WorkTimePolicy policy) {
        WorkTimePolicy existing =
                mapper.getGlobalPolicy(policy.getCustomerId());

        if (existing == null) {
            mapper.insertGlobalPolicy(policy);
        } else {
            mapper.updateGlobalPolicy(policy);
        }
    }

    @Override
    public List<WorkTimeUserOverride> getUserOverrides(int customerId) {
        return mapper.getUserOverrides(customerId);
    }

    @Override
    public WorkTimeUserOverride getUserOverride(int customerId, int userId) {
        return mapper.getUserOverride(customerId, userId);
    }

    @Override
    @Transactional
    public void saveUserOverride(WorkTimeUserOverride policy) {
        WorkTimeUserOverride existing = mapper.getUserOverride(policy.getCustomerId(), policy.getUserId());
        if (existing == null) {
            mapper.insertUserOverride(policy);
        } else {
            mapper.updateUserOverride(policy);
        }
    }

    @Override
    @Transactional
    public void deleteUserOverride(int customerId, int userId) {
        mapper.deleteUserOverride(customerId, userId);
    }

    @Override
    public List<WorkTimeUserOverride> getAllUserOverrides() {
        return mapper.getAllUserOverrides();
    }
}
