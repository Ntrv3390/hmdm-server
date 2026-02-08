package com.hmdm.plugins.worktime.service;

import java.util.Collections;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

public class EffectiveWorkTimePolicy {

    private final boolean enforcementEnabled;
    private final String startTime; // HH:mm
    private final String endTime;   // HH:mm
    private final int daysOfWeek; // bitmask 1..64
    private final Set<String> allowedDuring;
    private final Set<String> allowedOutside;

    public EffectiveWorkTimePolicy(boolean enforcementEnabled,
                                   String startTime,
                                   String endTime,
                                   int daysOfWeek,
                                   Set<String> allowedDuring,
                                   Set<String> allowedOutside) {
        this.enforcementEnabled = enforcementEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfWeek = daysOfWeek;
        this.allowedDuring = allowedDuring == null ? Collections.emptySet() : allowedDuring;
        this.allowedOutside = allowedOutside == null ? Collections.emptySet() : allowedOutside;
    }

    public boolean isEnforcementEnabled() {
        return enforcementEnabled;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getDaysOfWeek() {
        return daysOfWeek;
    }

    public Set<String> getAllowedDuring() {
        return allowedDuring;
    }

    public Set<String> getAllowedOutside() {
        return allowedOutside;
    }

    public boolean isWildcardAllowedDuring() {
        return allowedDuring.contains("*");
    }

    public boolean isWildcardAllowedOutside() {
        return allowedOutside.contains("*");
    }

    public boolean hasDay(DayOfWeek dow) {
        int mask = 1 << (dow.getValue() - 1);
        return (this.daysOfWeek & mask) == mask;
    }
}
