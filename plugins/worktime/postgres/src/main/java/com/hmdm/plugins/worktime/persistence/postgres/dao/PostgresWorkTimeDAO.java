package com.hmdm.plugins.worktime.persistence.postgres.dao;

import javax.inject.Inject;

import com.hmdm.plugins.worktime.model.WorkTimePolicy;
import com.hmdm.plugins.worktime.model.WorkTimeDeviceOverride;
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
    public List<WorkTimeDeviceOverride> getDeviceOverrides(int customerId) {
        return mapper.getDeviceOverrides(customerId);
    }

    @Override
    public WorkTimeDeviceOverride getDeviceOverride(int customerId, int deviceId) {
        return mapper.getDeviceOverride(customerId, deviceId);
    }

    @Override
    @Transactional
    public void saveDeviceOverride(WorkTimeDeviceOverride policy) {
        WorkTimeDeviceOverride existing = mapper.getDeviceOverride(policy.getCustomerId(), policy.getDeviceId());
        if (existing == null) {
            mapper.insertDeviceOverride(policy);
        } else {
            mapper.updateDeviceOverride(policy);
        }
    }

    @Override
    @Transactional
    public void deleteDeviceOverride(int customerId, int deviceId) {
        mapper.deleteDeviceOverride(customerId, deviceId);
    }

    @Override
    public List<WorkTimeDeviceOverride> getAllDeviceOverrides() {
        return mapper.getAllDeviceOverrides();
    }
}
