package com.hmdm.plugins.worktime.rest.resource;

import com.hmdm.plugins.worktime.service.EffectiveWorkTimePolicy;
import com.hmdm.plugins.worktime.service.WorkTimeService;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.Optional;

@Path("/plugins/worktime/public")
@Produces(MediaType.APPLICATION_JSON)
public class WorkTimePublicResource {

    private final WorkTimeService workTimeService;

    @Inject
    public WorkTimePublicResource(WorkTimeService workTimeService) {
        this.workTimeService = workTimeService;
    }

    private Integer resolveCustomerId(Integer customerIdParam) {
        Optional<com.hmdm.persistence.domain.User> u = SecurityContext.get().getCurrentUser();
        if (u.isPresent()) {
            return u.get().getCustomerId();
        }
        if (customerIdParam != null) return customerIdParam;
        throw new WebApplicationException("CustomerId required", 400);
    }

    @GET
    @Path("/policy/effective/{userId}")
    public Response getEffectivePolicy(@PathParam("userId") int userId, @QueryParam("customerId") Integer customerId) {
        int cid = resolveCustomerId(customerId);
        EffectiveWorkTimePolicy p = workTimeService.resolveEffectivePolicy(cid, userId, LocalDateTime.now());
        return Response.OK(p);
    }

    @GET
    @Path("/allowed")
    public Response isAppAllowed(@QueryParam("userId") Integer userId,
                                 @QueryParam("pkg") String pkg,
                                 @QueryParam("customerId") Integer customerId) {
        if (userId == null || pkg == null || pkg.trim().isEmpty()) {
            throw new WebApplicationException("userId and pkg query params required", 400);
        }
        int cid = resolveCustomerId(customerId);
        boolean allowed = workTimeService.isAppAllowed(cid, userId, pkg, LocalDateTime.now());
        return Response.OK(allowed);
    }
}
