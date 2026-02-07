package com.hmdm.plugins.worktime.persistence.postgres;

import com.google.inject.Module;
import com.hmdm.plugins.worktime.persistence.WorkTimePersistenceConfiguration;
import com.hmdm.plugins.worktime.persistence.postgres.guice.module.*;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

public class WorkTimePostgresPersistenceConfiguration implements WorkTimePersistenceConfiguration {

    public WorkTimePostgresPersistenceConfiguration() {
    }

    @Override
    public List<Module> getPersistenceModules(ServletContext context) {
        List<Module> modules = new ArrayList<>();

        modules.add(new WorkTimePostgresLiquibaseModule(context));
        modules.add(new WorkTimePostgresServiceModule());
        modules.add(new WorkTimePostgresPersistenceModule(context));

        return modules;
    }
}
