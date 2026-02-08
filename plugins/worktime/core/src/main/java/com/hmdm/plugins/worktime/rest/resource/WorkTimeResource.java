package com.hmdm.plugins.worktime.rest.resource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.hmdm.plugins.worktime.model.WorkTimePolicy;
import com.hmdm.plugins.worktime.model.WorkTimeUserOverride;
import com.hmdm.plugins.worktime.persistence.WorkTimeDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/plugins/worktime/private")
@Produces(MediaType.APPLICATION_JSON)
public class WorkTimeResource {

    private static final Logger log = LoggerFactory.getLogger(WorkTimeResource.class);

    private final WorkTimeDAO workTimeDAO;
    private final UserDAO userDAO;

    @Inject
    public WorkTimeResource(WorkTimeDAO workTimeDAO, UserDAO userDAO) {
        this.workTimeDAO = workTimeDAO;
        this.userDAO = userDAO;
    }

    private int getCustomerId() {
        return SecurityContext.get()
                .getCurrentUser()
                .orElseThrow(() -> new WebApplicationException("Unauthorized", 401))
                .getCustomerId();
    }

    // --- Global policy endpoints ---
    @GET
    @Path("/policy")
    public Response getPolicy() {
        // Check authentication
        User current = SecurityContext.get().getCurrentUser().orElse(null);
        if (current == null) {
            log.error("Unauthorized attempt to access worktime policy - not authenticated");
            return Response.PERMISSION_DENIED();
        }

        int customerId = getCustomerId();
        WorkTimePolicy policy = workTimeDAO.getGlobalPolicy(customerId);
        return Response.OK(policy);
    }

    @POST
    @Path("/policy")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response savePolicy(WorkTimePolicy policy) {
        // Check if user is admin or has worktime permission
        User current = SecurityContext.get().getCurrentUser().orElse(null);
        if (current == null) {
            log.error("Unauthorized attempt to save worktime policy - not authenticated");
            return Response.PERMISSION_DENIED();
        }
        
        if (!SecurityContext.get().isSuperAdmin() && !this.userDAO.isOrgAdmin(current)) {
            log.warn("User {} is not allowed to save policy: must be admin", current.getLogin());
            return Response.PERMISSION_DENIED();
        }

        int customerId = getCustomerId();
        policy.setCustomerId(customerId);
        workTimeDAO.saveGlobalPolicy(policy);
        return Response.OK(policy);
    }

    // --- User override endpoints (admin only) ---
    @GET
    @Path("/users")
    public Response getUserOverrides() {
        User current = SecurityContext.get().getCurrentUser().orElse(null);
        if (current == null) {
            log.error("Unauthorized attempt to access user overrides - not authenticated");
            return Response.PERMISSION_DENIED();
        }
        
        if (!SecurityContext.get().isSuperAdmin() && !this.userDAO.isOrgAdmin(current)) {
            log.warn("User {} is not allowed to list overrides: must be admin", current.getLogin());
            return Response.PERMISSION_DENIED();
        }

        int customerId = getCustomerId();
        
        // Get all users in the current customer's scope
        List<User> allUsers = userDAO.findAllUsers();
        
        // Get overrides for those users
        List<WorkTimeUserOverride> overrides = workTimeDAO.getUserOverrides(customerId);
        
        // Combine users with their overrides
        List<WorkTimeUserOverride> result = new java.util.ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        for (User user : allUsers) {
            WorkTimeUserOverride override = overrides.stream()
                    .filter(o -> o.getUserId() == user.getId())
                    .findFirst()
                    .orElse(null);
            
            if (override == null) {
                // Create a default override (no exceptions, enabled)
                override = new WorkTimeUserOverride();
                override.setCustomerId(customerId);
                override.setUserId(user.getId());
                override.setUserName(user.getLogin());
                override.setEnabled(true);
                override.setExceptions(new java.util.ArrayList<>());
            } else {
                override.setUserName(user.getLogin());
            }
            if (override.getExceptions() == null) {
                override.setExceptions(new java.util.ArrayList<>());
            }
            if (!override.isEnabled() && override.getStartDateTime() != null && override.getEndDateTime() != null) {
                LocalDateTime start = override.getStartDateTime().toLocalDateTime();
                LocalDateTime end = override.getEndDateTime().toLocalDateTime();
                if (now.isAfter(end)) {
                    workTimeDAO.deleteUserOverride(customerId, user.getId());
                    override.setExceptions(new java.util.ArrayList<>());
                    result.add(override);
                    continue;
                }
                boolean active = !now.isBefore(start) && !now.isAfter(end);
                Map<String, Object> ex = new java.util.HashMap<>();
                ex.put("dateFrom", start.toLocalDate().format(dateFmt));
                ex.put("dateTo", end.toLocalDate().format(dateFmt));
                ex.put("timeFrom", start.toLocalTime().format(timeFmt));
                ex.put("timeTo", end.toLocalTime().format(timeFmt));
                ex.put("active", active);
                override.getExceptions().add(ex);
            }
            result.add(override);
        }
        
        return Response.OK(result);
    }

    @GET
    @Path("/users/{userId}")
    public Response getUserOverride(@PathParam("userId") int userId) {
        User current = SecurityContext.get().getCurrentUser().orElse(null);
        if (current == null) {
            log.error("Unauthorized attempt to access user override: {} - not authenticated", userId);
            return Response.PERMISSION_DENIED();
        }
        
        if (!SecurityContext.get().isSuperAdmin() && !this.userDAO.isOrgAdmin(current)) {
            log.warn("User {} is not allowed to access override {}: must be admin", current.getLogin(), userId);
            return Response.PERMISSION_DENIED();
        }

        int customerId = getCustomerId();
        WorkTimeUserOverride override = workTimeDAO.getUserOverride(customerId, userId);
        return Response.OK(override);
    }

    @POST
    @Path("/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveUserOverride(@PathParam("userId") int userId, WorkTimeUserOverride override) {
        User current = SecurityContext.get().getCurrentUser().orElse(null);
        if (current == null) {
            log.error("Unauthorized attempt to save user override: {} - not authenticated", userId);
            return Response.PERMISSION_DENIED();
        }
        
        if (!SecurityContext.get().isSuperAdmin() && !this.userDAO.isOrgAdmin(current)) {
            log.warn("User {} is not allowed to save override {}: must be admin", current.getLogin(), userId);
            return Response.PERMISSION_DENIED();
        }

        int customerId = getCustomerId();
        if (override != null && !override.isEnabled()) {
            if (override.getStartDateTime() == null || override.getEndDateTime() == null) {
                return Response.ERROR("Exception start/end time required");
            }
            LocalDateTime start = override.getStartDateTime().toLocalDateTime();
            LocalDateTime end = override.getEndDateTime().toLocalDateTime();
            if (end.isBefore(start)) {
                return Response.ERROR("Exception end time must be after start time");
            }
            if (end.isBefore(LocalDateTime.now())) {
                return Response.ERROR("Exception end time must be in the future");
            }
        }
        override.setCustomerId(customerId);
        override.setUserId(userId);
        workTimeDAO.saveUserOverride(override);
        WorkTimeUserOverride saved = workTimeDAO.getUserOverride(customerId, userId);
        return Response.OK(saved != null ? saved : override);
    }

    @DELETE
    @Path("/users/{userId}")
    public Response deleteUserOverride(@PathParam("userId") int userId) {
        User current = SecurityContext.get().getCurrentUser().orElse(null);
        if (current == null) {
            log.error("Unauthorized attempt to delete user override: {} - not authenticated", userId);
            return Response.PERMISSION_DENIED();
        }
        
        if (!SecurityContext.get().isSuperAdmin() && !this.userDAO.isOrgAdmin(current)) {
            log.warn("User {} is not allowed to delete override {}: must be admin", current.getLogin(), userId);
            return Response.PERMISSION_DENIED();
        }

        int customerId = getCustomerId();
        workTimeDAO.deleteUserOverride(customerId, userId);
        return Response.OK();
    }
}
