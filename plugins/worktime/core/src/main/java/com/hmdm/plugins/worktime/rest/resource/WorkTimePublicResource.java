package com.hmdm.plugins.worktime.rest.resource;

import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.worktime.service.EffectiveWorkTimePolicy;
import com.hmdm.plugins.worktime.service.WorkTimeService;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * <p>Public REST resource for WorkTime plugin - accessible by Android devices.</p>
 * <p>These endpoints do not require admin authentication and can be called by MDM Android clients.</p>
 *
 * @author hmdm
 */
@Singleton
@Path("/plugins/worktime/public")
@Api(tags = {"WorkTime Plugin - Device APIs"})
@Produces(MediaType.APPLICATION_JSON)
public class WorkTimePublicResource {

    private static final Logger log = LoggerFactory.getLogger(WorkTimePublicResource.class);
    
    private final WorkTimeService workTimeService;
    private final UnsecureDAO unsecureDAO;

    @Inject
    public WorkTimePublicResource(WorkTimeService workTimeService, UnsecureDAO unsecureDAO) {
        this.workTimeService = workTimeService;
        this.unsecureDAO = unsecureDAO;
    }

    /**
     * Resolves customerId from authenticated user or query parameter.
     */
    private Integer resolveCustomerId(Integer customerIdParam) {
        Optional<com.hmdm.persistence.domain.User> u = SecurityContext.get().getCurrentUser();
        if (u.isPresent()) {
            return u.get().getCustomerId();
        }
        if (customerIdParam != null) return customerIdParam;
        throw new WebApplicationException("CustomerId required", 400);
    }

    // ==================================================================================
    // User-based endpoints (for admin panel or when userId is known)
    // ==================================================================================

    @GET
    @Path("/policy/effective/{userId}")
    @ApiOperation(
        value = "Get effective worktime policy for user",
        notes = "Returns the resolved worktime policy for specified user (merges global policy with user overrides)"
    )
    public Response getEffectivePolicy(
            @PathParam("userId") @ApiParam("User ID") int userId, 
            @QueryParam("customerId") @ApiParam("Customer ID (required if not authenticated)") Integer customerId) {
        try {
            int cid = resolveCustomerId(customerId);
            EffectiveWorkTimePolicy p = workTimeService.resolveEffectivePolicy(cid, userId, LocalDateTime.now());
            return Response.OK(p);
        } catch (Exception e) {
            log.error("Error getting effective policy for user {}", userId, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/allowed")
    @ApiOperation(
        value = "Check if app is allowed for user",
        notes = "Checks whether the specified app is allowed for the user at current server time"
    )
    public Response isAppAllowed(
            @QueryParam("userId") @ApiParam("User ID") Integer userId,
            @QueryParam("pkg") @ApiParam("App package name") String pkg,
            @QueryParam("customerId") @ApiParam("Customer ID") Integer customerId) {
        if (userId == null || pkg == null || pkg.trim().isEmpty()) {
            return Response.ERROR("userId and pkg query params required");
        }
        try {
            int cid = resolveCustomerId(customerId);
            boolean allowed = workTimeService.isAppAllowed(cid, userId, pkg, LocalDateTime.now());
            return Response.OK(allowed);
        } catch (Exception e) {
            log.error("Error checking if app {} is allowed for user {}", pkg, userId, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // ==================================================================================
    // Device-based endpoints (for Android MDM clients using deviceNumber)
    // ==================================================================================

    @GET
    @Path("/device/{deviceNumber}/policy")
    @ApiOperation(
        value = "Get worktime policy for device (Android client)",
        notes = "Returns the effective worktime policy for the specified device. " +
                "Android devices should use this endpoint to fetch their worktime policy."
    )
    public Response getDevicePolicy(
            @PathParam("deviceNumber") @ApiParam("Device number/ID from MDM") String deviceNumber) {
        try {
            // Resolve device by device number
            Device device = unsecureDAO.getDeviceByNumber(deviceNumber);
            if (device == null) {
                log.warn("Device {} not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            int customerId = device.getCustomerId();
            int deviceId = device.getId();
            
            // Use deviceId as userId for per-device policy resolution
            EffectiveWorkTimePolicy policy = workTimeService.resolveEffectivePolicy(
                customerId, 
                deviceId, 
                LocalDateTime.now()
            );

            log.debug("Returning worktime policy for device {}: enabled={}", deviceNumber, policy.isEnforcementEnabled());
            return Response.OK(policy);
            
        } catch (Exception e) {
            log.error("Error getting policy for device {}", deviceNumber, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/device/{deviceNumber}/allowed")
    @ApiOperation(
        value = "Check if app is allowed for device (Android client)",
        notes = "Checks whether the specified app is allowed on the device at current server time. " +
                "Returns true if allowed, false otherwise."
    )
    public Response isAppAllowedForDevice(
            @PathParam("deviceNumber") @ApiParam("Device number/ID from MDM") String deviceNumber,
            @QueryParam("pkg") @ApiParam("App package name to check") String pkg) {
        
        if (pkg == null || pkg.trim().isEmpty()) {
            return Response.ERROR("pkg query parameter required");
        }

        try {
            // Resolve device by device number
            Device device = unsecureDAO.getDeviceByNumber(deviceNumber);
            if (device == null) {
                log.warn("Device {} not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            int customerId = device.getCustomerId();
            int deviceId = device.getId();
            
            // Check if app is allowed for this device
            boolean allowed = workTimeService.isAppAllowed(
                customerId, 
                deviceId, 
                pkg, 
                LocalDateTime.now()
            );

            log.debug("App {} allowed for device {}: {}", pkg, deviceNumber, allowed);
            return Response.OK(allowed);
            
        } catch (Exception e) {
            log.error("Error checking if app {} is allowed for device {}", pkg, deviceNumber, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @GET
    @Path("/device/{deviceNumber}/status")
    @ApiOperation(
        value = "Get worktime enforcement status for device",
        notes = "Returns whether worktime enforcement is currently active for the device. " +
                "Useful for Android clients to check policy status without fetching full policy."
    )
    public Response getDeviceStatus(@PathParam("deviceNumber") @ApiParam("Device number/ID") String deviceNumber) {
        try {
            Device device = unsecureDAO.getDeviceByNumber(deviceNumber);
            if (device == null) {
                log.warn("Device {} not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            int customerId = device.getCustomerId();
            int deviceId = device.getId();
            
            EffectiveWorkTimePolicy policy = workTimeService.resolveEffectivePolicy(
                customerId, 
                deviceId, 
                LocalDateTime.now()
            );

            // Return status object with enabled flag and current work time status
            LocalDateTime now = LocalDateTime.now();
            boolean isWorkTime = workTimeService.isWorkTime(
                policy.getStartTime(), 
                policy.getEndTime(), 
                policy.getDaysOfWeek(), 
                now
            );

            WorkTimeStatus status = new WorkTimeStatus(
                policy.isEnforcementEnabled(),
                isWorkTime,
                policy.getStartTime(),
                policy.getEndTime(),
                policy.getDaysOfWeek()
            );

            return Response.OK(status);
            
        } catch (Exception e) {
            log.error("Error getting status for device {}", deviceNumber, e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * Status response object for Android clients.
     */
    public static class WorkTimeStatus {
        private boolean enabled;
        private boolean currentlyInWorkTime;
        private String startTime;
        private String endTime;
        private int daysOfWeek;

        public WorkTimeStatus() {}

        public WorkTimeStatus(boolean enabled, boolean currentlyInWorkTime, 
                            String startTime, String endTime, int daysOfWeek) {
            this.enabled = enabled;
            this.currentlyInWorkTime = currentlyInWorkTime;
            this.startTime = startTime;
            this.endTime = endTime;
            this.daysOfWeek = daysOfWeek;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isCurrentlyInWorkTime() { return currentlyInWorkTime; }
        public void setCurrentlyInWorkTime(boolean currentlyInWorkTime) { 
            this.currentlyInWorkTime = currentlyInWorkTime; 
        }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public int getDaysOfWeek() { return daysOfWeek; }
        public void setDaysOfWeek(int daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    }
}
