package com.hmdm.plugins.worktime.persistence.postgres.dao;

import javax.inject.Inject;

import com.hmdm.plugins.worktime.model.GlobalWorkTimePolicy;
import com.hmdm.plugins.worktime.persistence.WorkTimeDAO;
import com.hmdm.plugins.worktime.persistence.postgres.dao.mapper.PostgresWorkTimeMapper;
import org.mybatis.guice.transactional.Transactional;

public class PostgresWorkTimeDAO implements WorkTimeDAO {

    private final PostgresWorkTimeMapper mapper;

    @Inject
    public PostgresWorkTimeDAO(PostgresWorkTimeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GlobalWorkTimePolicy getGlobalPolicy(int customerId) {
        return mapper.getGlobalPolicy(customerId);
    }

    @Override
    @Transactional
    public void saveGlobalPolicy(GlobalWorkTimePolicy policy) {
        GlobalWorkTimePolicy existing =
                mapper.getGlobalPolicy(policy.getCustomerId());

        if (existing == null) {
            mapper.insertGlobalPolicy(policy);
        } else {
            mapper.updateGlobalPolicy(policy);
        }
    }
}
