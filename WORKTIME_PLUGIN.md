# Worktime Plugin - Complete Technical Documentation

## Table of Contents

1. [Overview](#overview)
2. [Purpose & Features](#purpose--features)
3. [Technology Stack](#technology-stack)
4. [Plugin Architecture](#plugin-architecture)
5. [Directory Structure](#directory-structure)
6. [Core Files & Classes](#core-files--classes)
7. [Database Schema](#database-schema)
8. [REST API Endpoints](#rest-api-endpoints)
9. [Configuration](#configuration)
10. [Integration Points](#integration-points)
11. [Development Guide](#development-guide)

---

## Overview

The **Worktime Plugin** is a Mobile Device Management (MDM) feature module for Headwind MDM that provides work time policy management capabilities. It allows administrators to define work-time restrictions and control app access on managed Android devices based on time-of-day and day-of-week rules.

### Plugin Identification

| Property           | Value                                  |
| ------------------ | -------------------------------------- |
| **Plugin ID**      | `worktime`                             |
| **Plugin Name**    | Work Time Policy Plugin for MDM Server |
| **Version**        | 0.1.0                                  |
| **Package**        | `com.hmdm.plugins.worktime`            |
| **Root Path**      | `hmdm-server/plugins/worktime/`        |

---

## Purpose & Features

### What It Does

The Worktime Plugin manages work-time policies for Android devices managed by Headwind MDM. It:

- **Defines work hours**: Allows setting start and end times for work periods
- **Restricts by day**: Specifies which days of the week the policy applies
- **Controls app access**: Defines which apps are allowed during work hours vs. outside work hours
- **Global policies**: Supports organization-wide work time policies
- **Per-customer configuration**: Each customer can have their own policies
- **Override capabilities**: Allows temporary overrides at global and per-user levels
- **Timezone support**: Handles different timezones for policy enforcement

### Use Cases

1. **Corporate Device Management**: Restrict personal apps during work hours
2. **BYOD (Bring Your Own Device)**: Control app access based on work time
3. **Compliance**: Enforce organizational policies for device usage
4. **Productivity**: Limit app usage outside work hours
5. **Security**: Control sensitive app access based on work schedule

---

## Technology Stack

### Backend Technologies

| Component            | Technology / Framework                                                                  |
| -------------------- | --------------------------------------------------------------------------------------- |
| **Language**         | Java 1.8+                                                                               |
| **Build System**     | Maven (multi-module architecture)                                                       |
| **Dependency Injection** | Google Guice (for IoC and module configuration)                                     |
| **REST Framework**   | Jersey 2.x (JAX-RS implementation)                                                      |
| **Database**         | PostgreSQL                                                                              |
| **Persistence**      | MyBatis (SQL mapping framework)                                                         |
| **Migrations**       | Liquibase (database versioning and schema management)                                   |
| **JSON Processing** | Jackson (Jackson Databind & Jackson Annotations)                                       |
| **API Documentation** | Swagger/Springfox annotations for OpenAPI documentation                                |

### Frontend Technologies

| Component      | Technology         |
| -------------- | ------------------ |
| **Framework**  | AngularJS 1.x      |
| **Templates**  | HTML5              |
| **Styling**    | Bootstrap / CSS    |
| **i18n**       | JSON-based (JSON)  |
| **Module Type** | AngularJS Module   |

### Core Dependencies

```xml
<!-- Platform base classes and utilities -->
<dependency>
    <groupId>com.hmdm.plugin</groupId>
    <artifactId>platform</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- Main server components -->
<dependency>
    <groupId>com.hmdm</groupId>
    <artifactId>server</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

## Plugin Architecture

The Worktime Plugin follows the standard two-module plugin architecture:

```
┌─────────────────────────────────────────────────────┐
│           CORE MODULE (Database-Agnostic)           │
├─────────────────────────────────────────────────────┤
│ • Model classes (domain objects)                    │
│ • DAO interfaces (data access contracts)            │
│ • REST resources (API endpoints)                    │
│ • Guice modules (dependency configuration)          │
│ • Core Liquibase changelog (plugin registration)    │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│      POSTGRES MODULE (Database-Specific)            │
├─────────────────────────────────────────────────────┤
│ • DAO implementations (PostgreSQL-specific)         │
│ • MyBatis mappers (SQL mapping)                     │
│ • Guice modules (persistence configuration)         │
│ • Postgres Liquibase changelog (table creation)     │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│       FRONTEND MODULE (AngularJS)                   │
├─────────────────────────────────────────────────────┤
│ • AngularJS module definition                       │
│ • HTML views/templates                              │
│ • i18n translation files                            │
│ • Controllers, directives, services                 │
└─────────────────────────────────────────────────────┘
```

---

## Directory Structure

### Complete Worktime Plugin Tree

```
hmdm-server/
├── plugins/
│   ├── pom.xml                                      # Parent POM (includes worktime module)
│   └── worktime/                                    # WORKTIME PLUGIN ROOT
│       ├── pom.xml                                  # Plugin parent POM
│       │
│       ├── core/                                    # CORE MODULE (Database-Agnostic)
│       │   ├── pom.xml                              # Core module POM
│       │   └── src/
│       │       └── main/
│       │           ├── java/
│       │           │   └── com/hmdm/plugins/worktime/
│       │           │       ├── WorkTimePluginConfigurationImpl.java     # [REQUIRED] Plugin entry point
│       │           │       │
│       │           │       ├── model/                                   # Domain Models
│       │           │       │   ├── WorkTimePolicy.java                # Work time policy definition
│       │           │       │   ├── WorkTimePolicyDeviceGroup.java     # Policy device group mapping
│       │           │       │   └── GlobalWorkTimePolicy.java          # Global policy settings
│       │           │       │
│       │           │       ├── persistence/                             # Data Access Layer
│       │           │       │   ├── WorkTimeDAO.java                   # DAO Interface (contract)
│       │           │       │   └── WorkTimePersistenceConfiguration.java # Persistence config interface
│       │           │       │
│       │           │       ├── rest/
│       │           │       │   └── resource/                            # REST Endpoints
│       │           │       │       └── WorkTimeResource.java           # REST resource class
│       │           │       │
│       │           │       └── guice/
│       │           │           └── module/                              # Guice Dependency Injection
│       │           │               ├── WorkTimeLiquibaseModule.java    # Liquibase DI
│       │           │               └── WorkTimeRestModule.java         # REST API DI
│       │           │
│       │           └── resources/
│       │               └── liquibase/
│       │                   └── worktime.changelog.xml  # Core changelog (plugin registration)
│       │
│       ├── postgres/                                # POSTGRES MODULE (Database-Specific)
│       │   ├── pom.xml                              # Postgres module POM
│       │   └── src/
│       │       ├── main/
│       │       │   ├── java/
│       │       │   │   └── com/hmdm/plugins/worktime/
│       │       │   │       └── persistence/postgres/
│       │       │   │           ├── WorkTimePostgresPersistenceConfiguration.java
│       │       │   │           │
│       │       │   │           ├── dao/                                 # DAO Implementations
│       │       │   │           │   ├── PostgresWorkTimeDAO.java        # PostgreSQL DAO impl
│       │       │   │           │   │
│       │       │   │           │   ├── domain/                          # DB-specific domain mapping
│       │       │   │           │   │   (optional - uses model classes)
│       │       │   │           │   │
│       │       │   │           │   └── mapper/                          # MyBatis SQL Mappers
│       │       │   │           │       ├── PostgresWorkTimeMapper.java  # Mapper interface
│       │       │   │           │       └── PostgresWorkTimeMapper.xml   # SQL XML mappings
│       │       │   │           │
│       │       │   │           └── guice/module/                        # Postgres DI
│       │       │   │               ├── WorkTimePostgresLiquibaseModule.java
│       │       │   │               ├── WorkTimePostgresPersistenceModule.java
│       │       │   │               └── WorkTimePostgresServiceModule.java
│       │       │   │
│       │       │   └── resources/
│       │       │       └── liquibase/
│       │       │           └── worktime.postgres.changelog.xml  # Table creation changelog
│       │       │
│       │       └── test/  (optional)
│       │           └── java/  (unit tests)
│       │
│       ├── src/                                     # FRONTEND MODULE (AngularJS)
│       │   └── main/
│       │       └── webapp/
│       │           ├── worktime.module.js           # AngularJS main module
│       │           │
│       │           ├── views/                       # HTML Templates
│       │           │   ├── main.html                # Main view
│       │           │   ├── settings.html            # Settings/policy view
│       │           │   └── (other views...)
│       │           │
│       │           └── i18n/                        # Internationalization
│       │               ├── en_US.json               # English translations
│       │               ├── ru_RU.json               # Russian translations
│       │               └── (other languages...)
│       │
│       └── target/  (ignored - Maven build output)
│           ├── classes/
│           ├── generated-sources/
│           └── ...
│
├── server/                                          # Main Server Module
│   ├── pom.xml                                      # <- MODIFIED: Includes worktime dependencies
│   ├── build.properties.example                     # <- MODIFIED: Plugin config class property
│   ├── build.properties                             # <- MODIFIED: Local plugin config
│   └── conf/
│       └── context.xml                              # <- MODIFIED: Plugin context parameter
│
└── install/
    └── context_template.xml                         # <- MODIFIED: Production deployment template
```

### Key Directories Summary

| Directory | Purpose | Location |
|-----------|---------|----------|
| **Core Java** | Business logic, models, DAOs | `plugins/worktime/core/src/main/java/` |
| **Postgres Java** | PostgreSQL implementations | `plugins/worktime/postgres/src/main/java/` |
| **Core Resources** | Liquibase migrations | `plugins/worktime/core/src/main/resources/liquibase/` |
| **Postgres Resources** | Core DB schema setup | `plugins/worktime/postgres/src/main/resources/liquibase/` |
| **Frontend** | AngularJS module & views | `plugins/worktime/src/main/webapp/` |
| **i18n** | Translation files | `plugins/worktime/src/main/webapp/i18n/` |
| **Build Output** | Compiled classes, JARs | `plugins/worktime/core/target/` & `postgres/target/` |

---

## Core Files & Classes

### 1. Plugin Entry Point

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/WorkTimePluginConfigurationImpl.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/WorkTimePluginConfigurationImpl.java)

```java
public class WorkTimePluginConfigurationImpl implements PluginConfiguration {
    
    public static final String PLUGIN_ID = "worktime";
    
    @Override
    public String getPluginId() {
        return PLUGIN_ID;  // Returns "worktime"
    }
    
    @Override
    public String getRootPackage() {
        return "com.hmdm.plugins.worktime";
    }
    
    @Override
    public List<Module> getPluginModules(ServletContext context) {
        // Loads Liquibase, Persistence, and REST modules
    }
}
```

**Responsibilities:**
- Serves as the main plugin entry point
- Initializes Guice modules for dependency injection
- Loads persistence configuration
- Registers REST endpoints

---

### 2. Model Classes

#### 2.1 WorkTimePolicy

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/model/WorkTimePolicy.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/model/WorkTimePolicy.java)

Represents a work time policy definition:

```
Properties:
├── id: Integer                          # Unique policy identifier
├── name: String                         # Policy name
├── description: String                  # Policy description
├── startTime: String                    # Start time (HH:mm format)
├── endTime: String                      # End time (HH:mm format)
├── daysOfWeek: Integer                  # Bitmask of days (0-127)
├── allowedAppsDuringWork: String        # Apps allowed during work
├── allowedAppsOutsideWork: String       # Apps allowed outside work
├── priority: Integer                    # Policy priority
├── timezone: String                     # Timezone (e.g., Europe/Bucharest)
├── customerId: Integer                  # Customer/organization ID
├── createdAt: Date                      # Creation timestamp
├── updatedAt: Date                      # Last update timestamp
├── createdBy: Integer                   # User ID who created policy
└── deviceGroups: List<WorkTimePolicyDeviceGroup>  # Associated device groups
```

#### 2.2 GlobalWorkTimePolicy

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/model/GlobalWorkTimePolicy.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/model/GlobalWorkTimePolicy.java)

Organization-wide global policy (simpler structure):

```
Properties:
├── id: Integer                          # Policy ID
├── startTime: String                    # Start time (HH:mm)
├── endTime: String                      # End time (HH:mm)
├── daysOfWeek: Integer                  # Days bitmask
├── allowedAppsDuringWork: String        # Allowed apps during work
├── allowedAppsOutsideWork: String       # Allowed apps outside work
├── enabled: Boolean                     # Enable/disable policy
└── customerId: Integer                  # Customer ID
```

#### 2.3 WorkTimePolicyDeviceGroup

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/model/WorkTimePolicyDeviceGroup.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/model/WorkTimePolicyDeviceGroup.java)

Maps policies to device groups:

```
Properties:
├── id: Integer                          # Mapping ID
├── workTimePolicyId: Integer            # Policy ID
├── deviceGroupId: Integer               # Device group ID
└── customerId: Integer                  # Customer ID
```

---

### 3. Data Access Layer

#### 3.1 WorkTimeDAO Interface

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/persistence/WorkTimeDAO.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/persistence/WorkTimeDAO.java)

Defines data access contracts (database-agnostic):

```java
public interface WorkTimeDAO {
    GlobalWorkTimePolicy getGlobalPolicy(int customerId);
    void saveGlobalPolicy(GlobalWorkTimePolicy policy);
}
```

#### 3.2 PostgresWorkTimeDAO

**File:** [plugins/worktime/postgres/src/main/java/com/hmdm/plugins/worktime/persistence/postgres/dao/PostgresWorkTimeDAO.java](plugins/worktime/postgres/src/main/java/com/hmdm/plugins/worktime/persistence/postgres/dao/PostgresWorkTimeDAO.java)

PostgreSQL-specific implementation of WorkTimeDAO.

#### 3.3 MyBatis Mapper

**Files:**
- Mapper Interface: [plugins/worktime/postgres/src/main/java/com/hmdm/plugins/worktime/persistence/postgres/dao/mapper/PostgresWorkTimeMapper.java](plugins/worktime/postgres/src/main/java/com/hmdm/plugins/worktime/persistence/postgres/dao/mapper/PostgresWorkTimeMapper.java)
- SQL Mappings: [plugins/worktime/postgres/src/main/java/com/hmdm/plugins/worktime/persistence/postgres/dao/mapper/PostgresWorkTimeMapper.xml](plugins/worktime/postgres/src/main/java/com/hmdm/plugins/worktime/persistence/postgres/dao/mapper/PostgresWorkTimeMapper.xml)

---

### 4. REST Resources

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/rest/resource/WorkTimeResource.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/rest/resource/WorkTimeResource.java)

REST endpoint resource class (see [REST API Endpoints](#rest-api-endpoints) section for details).

---

### 5. Guice Modules

#### 5.1 WorkTimeRestModule

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/guice/module/WorkTimeRestModule.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/guice/module/WorkTimeRestModule.java)

Configures REST API security filters:

```java
public class WorkTimeRestModule extends ServletModule {
    // Protected resources requiring authentication
    private static final List<String> protectedResources = 
        Arrays.asList("/rest/plugins/worktime/*");
    
    // Applied filters:
    // - JWTFilter (JWT token validation)
    // - AuthFilter (authentication)
    // - PluginAccessFilter (plugin access control)
    // - PrivateIPFilter (IP whitelist validation)
}
```

#### 5.2 WorkTimeLiquibaseModule

**File:** [plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/guice/module/WorkTimeLiquibaseModule.java](plugins/worktime/core/src/main/java/com/hmdm/plugins/worktime/guice/module/WorkTimeLiquibaseModule.java)

Configures Liquibase for database migrations:

```java
public class WorkTimeLiquibaseModule extends AbstractLiquibaseModule {
    @Override
    protected String getChangeLogResourcePath() {
        return "jar:.../liquibase/worktime.changelog.xml";
    }
}
```

#### 5.3 Postgres-Specific Modules

Three modules in postgres package:

| Module | File | Purpose |
|--------|------|---------|
| **WorkTimePostgresLiquibaseModule** | `postgres/.../guice/module/WorkTimePostgresLiquibaseModule.java` | Loads postgres-specific Liquibase changelog |
| **WorkTimePostgresPersistenceModule** | `postgres/.../guice/module/WorkTimePostgresPersistenceModule.java` | Configures MyBatis mappers |
| **WorkTimePostgresServiceModule** | `postgres/.../guice/module/WorkTimePostgresServiceModule.java` | Binds DAO implementations to interfaces |

---

## Database Schema

### Tables Created by Liquibase

All tables are created by [plugins/worktime/postgres/src/main/resources/liquibase/worktime.postgres.changelog.xml](plugins/worktime/postgres/src/main/resources/liquibase/worktime.postgres.changelog.xml)

#### 1. worktime_global_policy

Organization-wide work time policy:

| Column | Type | Nullable | Default | Purpose |
|--------|------|----------|---------|---------|
| `id` | SERIAL | NO | (PK) | Policy identifier |
| `start_time` | VARCHAR(5) | NO | | Start time (HH:mm) |
| `end_time` | VARCHAR(5) | NO | | End time (HH:mm) |
| `days_of_week` | INT | NO | | Bitmask (0-127): 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun |
| `allowed_apps_during_work` | TEXT | YES | | Comma-separated app packages or `*` for all |
| `allowed_apps_outside_work` | TEXT | YES | | Comma-separated app packages or `*` for all |
| `enabled` | BOOLEAN | NO | true | Enable/disable policy |
| `customer_id` | INT | NO | | Organization/customer ID (FK to customers table) |

#### 2. worktime_global_override

Temporary organizational overrides:

| Column | Type | Nullable | Default | Purpose |
|--------|------|----------|---------|---------|
| `id` | SERIAL | NO | (PK) | Override identifier |
| `start_datetime` | TIMESTAMP | YES | | Override start date/time |
| `end_datetime` | TIMESTAMP | YES | | Override end date/time |
| `enabled` | BOOLEAN | NO | true | Enable/disable override |
| `customer_id` | INT | NO | | Organization ID |

#### 3. worktime_user_override

Per-user exceptions to policies:

| Column | Type | Nullable | Default | Purpose |
|--------|------|----------|---------|---------|
| `id` | SERIAL | NO | (PK) | Override identifier |
| `user_id` | INT | YES | | User ID (if applicable) |
| `device_id` | INT | YES | | Device ID (if applicable) |
| `start_datetime` | TIMESTAMP | YES | | Override start date/time |
| `end_datetime` | TIMESTAMP | YES | | Override end date/time |
| `enabled` | BOOLEAN | NO | true | Enable/disable override |
| `customer_id` | INT | NO | | Organization ID |

### Liquibase Changelogs

| File | Location | Purpose |
|------|----------|---------|
| **Core Changelog** | `core/src/main/resources/liquibase/worktime.changelog.xml` | Plugin registration (intentionally empty - delegates to postgres) |
| **Postgres Changelog** | `postgres/src/main/resources/liquibase/worktime.postgres.changelog.xml` | Creates all worktime tables |

---

## REST API Endpoints

### Base URL

```
http://<host>:<port>/rest/plugins/worktime/
```

### Authentication

All endpoints require:
- **JWT Token**: Valid JWT authentication token
- **Admin Access**: Must have admin privileges for the customer
- **IP Whitelist**: Internal IP addresses only

### Endpoints

#### 1. GET /rest/plugins/worktime/private/policy

**Description:** Retrieve the global work time policy for the current customer

**Authentication:** Required (JWT + Auth Filter)

**Request:**
```
GET /rest/plugins/worktime/private/policy
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "startTime": "09:00",
    "endTime": "17:00",
    "daysOfWeek": 62,  // Mon-Fri (2+4+8+16+32 = 62)
    "allowedAppsDuringWork": "com.company.mail,com.company.calendar",
    "allowedAppsOutsideWork": "*",
    "enabled": true,
    "customerId": 123
  }
}
```

**Status Codes:**
- `200 OK` - Policy retrieved successfully
- `401 Unauthorized` - Invalid or missing JWT token
- `403 Forbidden` - User lacks required permissions
- `404 Not Found` - No policy configured for customer

---

#### 2. POST /rest/plugins/worktime/private/policy

**Description:** Create or update the global work time policy

**Authentication:** Required (JWT + Auth Filter)

**Content-Type:** `application/json`

**Request:**
```json
POST /rest/plugins/worktime/private/policy
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "id": 1,
  "startTime": "09:00",
  "endTime": "17:00",
  "daysOfWeek": 62,
  "allowedAppsDuringWork": "com.company.mail,com.company.calendar",
  "allowedAppsOutsideWork": "*",
  "enabled": true,
  "customerId": 123
}
```

**Response:**
```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "startTime": "09:00",
    "endTime": "17:00",
    "daysOfWeek": 62,
    "allowedAppsDuringWork": "com.company.mail,com.company.calendar",
    "allowedAppsOutsideWork": "*",
    "enabled": true,
    "customerId": 123
  }
}
```

**Status Codes:**
- `200 OK` - Policy saved successfully
- `400 Bad Request` - Invalid policy data
- `401 Unauthorized` - Invalid or missing JWT token
- `403 Forbidden` - User lacks required permissions

---

### Days of Week Bitmask Reference

The `daysOfWeek` field uses a 7-bit bitmask:

| Day | Bit Value | Hex |
|-----|-----------|-----|
| Monday | 1 | 0x01 |
| Tuesday | 2 | 0x02 |
| Wednesday | 4 | 0x04 |
| Thursday | 8 | 0x08 |
| Friday | 16 | 0x10 |
| Saturday | 32 | 0x20 |
| Sunday | 64 | 0x40 |

**Common Values:**
- Monday-Friday: `1+2+4+8+16 = 31` or `0x1F`
- Monday-Friday (Revised): `2+4+8+16+32 = 62` or `0x3E`
- All days: `127` or `0x7F`
- Weekday + Saturday: `1+2+4+8+16+32 = 63` or `0x3F`

---

## Configuration

### 1. Project-Level Configuration

#### 1.1 `plugins/pom.xml`

Add worktime module to parent POM:

**Location:** [plugins/pom.xml](plugins/pom.xml)

```xml
<modules>
    <module>platform</module>
    <module>devicelog</module>
    <module>deviceinfo</module>
    <module>audit</module>
    <module>messaging</module>
    <module>push</module>
    <module>worktime</module>  <!-- ADD THIS -->
    <module>xtra</module>
</modules>
```

---

#### 1.2 `server/pom.xml`

Add runtime dependencies for worktime core and postgres modules:

**Location:** [server/pom.xml](server/pom.xml)

```xml
<dependencies>
    <!-- Other dependencies... -->
    
    <!-- ADD THESE DEPENDENCIES -->
    <dependency>
        <groupId>com.hmdm.plugin</groupId>
        <artifactId>worktime-core</artifactId>
        <version>0.1.0</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.hmdm.plugin</groupId>
        <artifactId>worktime-postgres</artifactId>
        <version>0.1.0</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

### 2. Build-Time Configuration

#### 2.1 `server/build.properties.example`

Define the persistence configuration class:

**Location:** [server/build.properties.example](server/build.properties.example)

```properties
# Configure worktime plugin persistence
plugin.worktime.persistence.config.class=com.hmdm.plugins.worktime.persistence.postgres.WorkTimePostgresPersistenceConfiguration
```

#### 2.2 `server/build.properties` (Local)

Copy the property to your local `build.properties`:

```properties
# Same as above
plugin.worktime.persistence.config.class=com.hmdm.plugins.worktime.persistence.postgres.WorkTimePostgresPersistenceConfiguration
```

---

### 3. Runtime Configuration

#### 3.1 Development: `server/conf/context.xml`

Add Tomcat context parameter:

**Location:** [server/conf/context.xml](server/conf/context.xml)

```xml
<Context>
    <!-- Other parameters... -->
    
    <!-- ADD THIS PARAMETER -->
    <Parameter name="plugin.worktime.persistence.config.class"
               value="${plugin.worktime.persistence.config.class}"/>
</Context>
```

**Purpose:** Passes persistence configuration class to servlet context for runtime initialization

---

#### 3.2 Production: `install/context_template.xml`

Add for production deployments:

**Location:** [install/context_template.xml](install/context_template.xml)

```xml
<Context>
    <!-- Other parameters... -->
    
    <!-- ADD THIS PARAMETER (hardcoded for production) -->
    <Parameter name="plugin.worktime.persistence.config.class"
               value="com.hmdm.plugins.worktime.persistence.postgres.WorkTimePostgresPersistenceConfiguration"/>
</Context>
```

**Purpose:** Production deployment template uses hardcoded class name (no Maven filtering)

---

## Integration Points

### 1. Plugin Discovery & Loading

The plugin is discovered and loaded by the main server via:

1. **Service Provider Interface (SPI):** Java's `ServiceLoader` mechanism
2. **File:** `META-INF/services/com.hmdm.plugin.PluginConfiguration`
3. **Entry:** Points to `com.hmdm.plugins.worktime.WorkTimePluginConfigurationImpl`

### 2. Dependency Injection

The plugin integrates with Headwind MDM's Guice-based DI container:

```
Server Boot
  ↓
Load PluginConfiguration implementations (SPI)
  ↓
Call WorkTimePluginConfigurationImpl.getPluginModules()
  ↓
Install Guice modules:
  - WorkTimeLiquibaseModule (DB migrations)
  - WorkTimePostgresLiquibaseModule (Postgres specific)
  - WorkTimePostgresServiceModule (DAO binding)
  - WorkTimePostgresPersistenceModule (MyBatis)
  - WorkTimeRestModule (REST API binding)
  ↓
Bind REST resources:
  - WorkTimeResource (@Path /rest/plugins/worktime/*)
```

### 3. Database Integration

Liquibase automatically runs migrations:

```
Server Startup
  ↓
Liquibase initialization
  ↓
Load changelog from classpath
  ↓
Execute changesets:
  - worktime.changelog.xml (core - empty)
  - worktime.postgres.changelog.xml (creates tables)
  ↓
Database ready with worktime tables
```

### 4. REST API Integration

Jersey/JAX-RS discovers REST resources:

```
HTTP Request: GET /rest/plugins/worktime/private/policy
  ↓
Jersey routing (path matching)
  ↓
Filter chain execution:
  1. JWTFilter (validate JWT)
  2. AuthFilter (check authentication)
  3. PluginAccessFilter (plugin permissions)
  4. PrivateIPFilter (IP validation)
  ↓
WorkTimeResource.getDemoPolicy()
  ↓
WorkTimeDAO implementation call
  ↓
MyBatis mapper execution
  ↓
PostgreSQL query
  ↓
JSON response serialization
```

---

## Development Guide

### Build the Plugin

Navigate to plugin root and build with Maven:

```bash
cd hmdm-server/plugins/worktime
mvn clean install
```

This builds:
- `worktime-core-0.1.0.jar`
- `worktime-postgres-0.1.0.jar`

### Build Entire Project

```bash
cd hmdm-server
mvn clean install
```

### Run Tests

```bash
# Run plugin tests only
mvn test -DskipTests=false

# Run full project tests
cd hmdm-server
mvn test
```

### Debug Information

#### Enable Debug Logging

Add to Logback configuration (`conf/logback.xml` or similar):

```xml
<logger name="com.hmdm.plugins.worktime" level="DEBUG"/>
```

#### Check Loaded Plugins

The server logs all discovered plugins on startup:

```
INFO  Discovered PluginConfiguration implementations:
INFO  - com.hmdm.plugins.platform.PlatformPluginConfigurationImpl
INFO  - com.hmdm.plugins.worktime.WorkTimePluginConfigurationImpl
INFO  - ...
```

#### Verify REST Endpoints Bound

Jersey logs bound resources:

```
INFO  Binding REST resource: com.hmdm.plugins.worktime.rest.resource.WorkTimeResource
INFO  Path: /rest/plugins/worktime/private
```

### Common Development Tasks

#### Add a New REST Endpoint

1. Add method to `WorkTimeResource`:
   ```java
   @GET
   @Path("/newendpoint")
   public Response getNewEndpoint() {
       // Implementation
   }
   ```

2. Method is auto-discovered by Jersey (no additional configuration needed)

#### Add a New Database Table

1. Create new changeset in `postgres/src/main/resources/liquibase/worktime.postgres.changelog.xml`:
   ```xml
   <changeSet id="worktime-create-newtable" author="worktime-plugin">
       <createTable tableName="worktime_newtable">
           <!-- columns -->
       </createTable>
   </changeSet>
   ```

2. Liquibase automatically executes on next server startup

#### Update a Data Model

1. Modify class in `core/src/main/java/com/hmdm/plugins/worktime/model/`
2. Update corresponding MyBatis mapper XML if needed
3. Add Liquibase migration for schema changes
4. Rebuild and redeploy

---

## Summary: Key Paths & Files

| Element | Path |
|---------|------|
| **Plugin Root** | `hmdm-server/plugins/worktime/` |
| **Core Module** | `plugins/worktime/core/` |
| **Postgres Module** | `plugins/worktime/postgres/` |
| **Frontend** | `plugins/worktime/src/main/webapp/` |
| **Entry Point** | `core/src/main/java/.../WorkTimePluginConfigurationImpl.java` |
| **DAO Interface** | `core/src/main/java/.../persistence/WorkTimeDAO.java` |
| **DAO Implementation** | `postgres/src/main/java/.../dao/PostgresWorkTimeDAO.java` |
| **REST Resource** | `core/src/main/java/.../rest/resource/WorkTimeResource.java` |
| **Core Changelog** | `core/src/main/resources/liquibase/worktime.changelog.xml` |
| **Postgres Changelog** | `postgres/src/main/resources/liquibase/worktime.postgres.changelog.xml` |
| **Models** | `core/src/main/java/.../model/` |
| **Guice Modules** | `core/src/main/java/.../guice/module/` |
| **MyBatis Mappers** | `postgres/src/main/java/.../dao/mapper/` |
| **i18n Translations** | `src/main/webapp/i18n/` |
| **Server Integration** | `server/pom.xml`, `server/build.properties`, `server/conf/context.xml` |
| **Installation Template** | `install/context_template.xml` |

---

## Appendix: Plugin ID & Constants

```java
// Plugin Identifier
PLUGIN_ID = "worktime"

// Package Name
ROOT_PACKAGE = "com.hmdm.plugins.worktime"

// REST Base Paths
PROTECTED_PATH = "/rest/plugins/worktime/private"
PUBLIC_PATH = "/rest/plugins/worktime/public"  (optional)

// Database Tables
TABLE_GLOBAL_POLICY = "worktime_global_policy"
TABLE_GLOBAL_OVERRIDE = "worktime_global_override"
TABLE_USER_OVERRIDE = "worktime_user_override"

// Configuration Parameter
CONFIG_PARAM = "plugin.worktime.persistence.config.class"
CONFIG_CLASS = "com.hmdm.plugins.worktime.persistence.postgres.WorkTimePostgresPersistenceConfiguration"
```

---

## Related Resources

- **Main Plugin Development Guide:** [createPlugin.md](createPlugin.md)
- **Server Code:** [server/](server/)
- **Platform Plugin:** [plugins/platform/](plugins/platform/)
- **Build Configuration:** [pom.xml](pom.xml)
- **Installation Scripts:** [hmdm_install.sh](hmdm_install.sh)

---

**Document Version:** 1.0  
**Last Updated:** February 2026  
**Worktime Plugin Version:** 0.1.0
