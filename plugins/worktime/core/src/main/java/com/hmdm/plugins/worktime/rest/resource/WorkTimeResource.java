package com.hmdm.plugins.worktime.rest.resource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.hmdm.plugins.worktime.model.GlobalWorkTimePolicy;
import com.hmdm.plugins.worktime.persistence.WorkTimeDAO;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;

@Path("/plugins/worktime/private")
@Produces(MediaType.APPLICATION_JSON)
public class WorkTimeResource {

    private final WorkTimeDAO workTimeDAO;

    @Inject
    public WorkTimeResource(WorkTimeDAO workTimeDAO) {
        this.workTimeDAO = workTimeDAO;
    }

    private int getCustomerId() {
        return SecurityContext.get()
                .getCurrentUser()
                .orElseThrow(() -> new WebApplicationException("Unauthorized", 401))
                .getCustomerId();
    }

    @GET
    @Path("/policy")
    public Response getPolicy() {
        int customerId = getCustomerId();

        GlobalWorkTimePolicy policy =
                workTimeDAO.getGlobalPolicy(customerId);

        return Response.OK(policy);
    }

    @POST
    @Path("/policy")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response savePolicy(GlobalWorkTimePolicy policy) {
        int customerId = getCustomerId();

        policy.setCustomerId(customerId);

        workTimeDAO.saveGlobalPolicy(policy);
        return Response.OK(policy);
    }
}
