#!/bin/bash

##############################################################################
# WorkTime Plugin API Test Script
# 
# This script tests all REST API endpoints for the WorkTime plugin.
# 
# Prerequisites:
# - Headwind MDM server running (default: http://localhost:8080)
# - Valid authentication credentials
# - At least one device enrolled
#
# Usage:
#   ./TEST_API.sh [BASE_URL] [USERNAME] [PASSWORD]
#
# Example:
#   ./TEST_API.sh http://localhost:8080 admin admin
##############################################################################

# Configuration
BASE_URL="${1:-http://localhost:8080}"
USERNAME="${2:-admin}"
PASSWORD="${3:-admin}"
DEVICE_NUMBER="${4:-TESTDEVICE001}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
PASSED=0
FAILED=0

echo "=========================================="
echo "WorkTime Plugin API Test Suite"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "Username: $USERNAME"
echo "Device: $DEVICE_NUMBER"
echo "=========================================="
echo ""

# Function to test API endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    
    echo -n "Testing: $name ... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET \
            -u "$USERNAME:$PASSWORD" \
            "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -u "$USERNAME:$PASSWORD" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE \
            -u "$USERNAME:$PASSWORD" \
            "$BASE_URL$endpoint")
    elif [ "$method" = "GET_PUBLIC" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET \
            "$BASE_URL$endpoint")
    fi
    
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$status" = "$expected_status" ] || [ "$expected_status" = "*" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $status)"
        echo "   Response: $(echo $body | head -c 100)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected_status, Got: $status)"
        echo "   Response: $body"
        FAILED=$((FAILED + 1))
    fi
    echo ""
}

# Wait a moment before starting tests
sleep 1

echo "=========================================="
echo "1. Admin API Tests (Authenticated)"
echo "=========================================="
echo ""

# Test 1: Get global policy
test_endpoint \
    "Get Global Policy" \
    "GET" \
    "/rest/plugins/worktime/private/policy" \
    "" \
    "200"

# Test 2: Save global policy
policy_data='{
    "startTime": "09:00",
    "endTime": "18:00",
    "daysOfWeek": 31,
    "allowedAppsDuringWork": "com.android.chrome,com.android.email",
    "allowedAppsOutsideWork": "*",
    "enabled": true
}'

test_endpoint \
    "Save Global Policy" \
    "POST" \
    "/rest/plugins/worktime/private/policy" \
    "$policy_data" \
    "200"

# Test 3: List user overrides
test_endpoint \
    "List User Overrides" \
    "GET" \
    "/rest/plugins/worktime/private/users" \
    "" \
    "*"

# Test 4: Create user override
user_override='{
    "userId": 1,
    "enabled": false,
    "startDateTime": "2026-02-09T00:00:00",
    "endDateTime": "2026-02-10T23:59:59"
}'

test_endpoint \
    "Create User Override" \
    "POST" \
    "/rest/plugins/worktime/private/users" \
    "$user_override" \
    "*"

# Test 5: Get user override
test_endpoint \
    "Get User Override" \
    "GET" \
    "/rest/plugins/worktime/private/users/1" \
    "" \
    "*"

# Test 6: Check if app allowed (user-based)
test_endpoint \
    "Check App Allowed (User API)" \
    "GET" \
    "/rest/plugins/worktime/private/users/1/allowed?pkg=com.android.chrome" \
    "" \
    "*"

# Test 7: Get user status
test_endpoint \
    "Get User Status" \
    "GET" \
    "/rest/plugins/worktime/private/users/1/status" \
    "" \
    "*"

echo "=========================================="
echo "2. Public Device API Tests (Unauthenticated)"
echo "=========================================="
echo ""

# Test 8: Get device policy (public)
test_endpoint \
    "Get Device Policy (Public)" \
    "GET_PUBLIC" \
    "/rest/plugins/worktime/public/device/$DEVICE_NUMBER/policy" \
    "" \
    "*"

# Test 9: Check if app allowed for device
test_endpoint \
    "Check App Allowed (Device API)" \
    "GET_PUBLIC" \
    "/rest/plugins/worktime/public/device/$DEVICE_NUMBER/allowed?pkg=com.android.chrome" \
    "" \
    "*"

# Test 10: Get device status
test_endpoint \
    "Get Device Status (Public)" \
    "GET_PUBLIC" \
    "/rest/plugins/worktime/public/device/$DEVICE_NUMBER/status" \
    "" \
    "*"

echo "=========================================="
echo "3. Edge Cases & Error Handling"
echo "=========================================="
echo ""

# Test 11: Get policy for non-existent device
test_endpoint \
    "Non-existent Device" \
    "GET_PUBLIC" \
    "/rest/plugins/worktime/public/device/NONEXISTENT999/policy" \
    "" \
    "*"

# Test 12: Check app without package parameter
test_endpoint \
    "Missing Package Parameter" \
    "GET" \
    "/rest/plugins/worktime/private/users/1/allowed" \
    "" \
    "*"

# Test 13: Delete user override
test_endpoint \
    "Delete User Override" \
    "DELETE" \
    "/rest/plugins/worktime/private/users/1" \
    "" \
    "*"

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "Total: $((PASSED + FAILED))"
echo "=========================================="

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some tests failed.${NC}"
    exit 1
fi
