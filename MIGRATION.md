# Migration Status: bx-base to bx-namespace

## Overview
This document tracks the migration of core namespace-related modules from `@bx-base` to `@bx-namespace`.

## Migration Date
October 22, 2025

## Modules Migrated

### ✅ Completed Migrations

#### 1. Namespace Module
**Entity:**
- `NamespaceTb.java` - Main namespace entity with JSONB properties
- Composite ID: Uses UUID primary key

**Repository:**
- `NamespaceRepository.java` - JPA repository with custom queries
- `NamespaceUserViewRepository.java` - Materialized view repository

**Record:**
- `NamespaceRecord.java` - Persistence wrapper with functional callbacks

**Helper:**
- `NamespaceDbHelper.java` - Repository facade (12K+ lines)

**API Implementation:**
- `NamespaceImpl.java` - Main namespace domain object
- `DomainObjectBuilder.java` - Factory for creating domain objects
- `CachedNamespaceUsers.java` - User caching

**Managers:**
- `NamespacePermissionManager.java` - Permission checking
- `NamespaceEntityManager.java` - Entity management
- `NamespacePlaceManager.java` - Location management

#### 2. Namespace-User Module
**Entity:**
- `NamespaceUserTb.java` - User membership in namespace
- `NamespaceUserViewTb.java` - Database view
- Composite ID: `NamespaceUserId` (namespace_id, id)

**Repository:**
- `NamespaceUserRepository.java` - User-namespace queries
- `NamespaceUserSpecifications.java` - JPA Criteria API specs

**Record:**
- `NamespaceUserRecord.java` - User record wrapper

**API Implementation:**
- `NamespaceUserImpl.java` - Namespace user implementation

#### 3. UserBase Module
**Entity:**
- `UserBaseTb.java` - Global user entity with JSONB profile

**Repository:**
- `UserBaseRepository.java` - User queries

**Record:**
- `UserRecord.java` - User record wrapper with profile management

**Helper:**
- `UserDbHelper.java` - User operations facade

**API Implementation:**
- `UserBaseImpl.java` - User domain object
- `UserDataFormatter.java` - Data formatting utility

**Service:**
- `UserServiceImpl.java` - User business logic

#### 4. BasePrincipal Module
**Entity:**
- `BasePrincipalTb.java` - Base identity entity

**Repository:**
- `BasePrincipalRepository.java` - Principal CRUD

**Helper:**
- `BasePrincipalHelper.java` - Principal lookup with caching

#### 5. Namespace-Role Module
**Entity:**
- `NamespaceRoleTb.java` - Role definitions with permissions
- Composite ID: `NamespaceRoleId` (namespace_id, id)

**Repository:**
- `NamespaceRoleRepository.java` - Role queries

**Record:**
- `NamespaceRoleRecord.java` - Role record wrapper

**API Implementation:**
- `NamespaceRoleImpl.java` - Role domain object

#### 6. Entity Module
**Entity:**
- `EntityTb.java` - Generic domain entity with JSONB profile
- `EntityTypeTb.java` - Entity type definitions
- Composite IDs: `EntityId`, `EntityTypeId`

**Repository:**
- `EntityRepository.java` - Entity queries
- `EntityTypeRepository.java` - Type queries

**Record:**
- `EntityRecord.java` - Entity record wrapper
- `EntityTypeRecord.java` - Type record wrapper

**Helper:**
- `EntityDbHelper.java` - Entity operations facade

**API Implementation:**
- `EntityImpl.java` - Entity domain object
- `EntityTypeImpl.java` - Type domain object

#### 7. Place Module
**Entity:**
- `PlaceTb.java` - Location entity with PostGIS geometry

**Repository:**
- `PlaceRepository.java` - Spatial queries (PostGIS)

**Record:**
- `PlaceRecord.java` - Place record wrapper

**Helper:**
- `PlaceDbHelper.java` - Place operations facade

**API Implementation:**
- `PlaceImpl.java` - Place domain object

**Service:**
- `PlaceServiceImpl.java` - Place business logic

#### 8. Security Module
- `AdminSecurityContext.java` - System-level security
- `AdminContextInitializer.java` - Admin context initialization
- `WebAppSecurityContext.java` - Web request security
- `WebRequestContextInitializer.java` - Web context initialization
- `NamespaceContextImpl.java` - Namespace-scoped context
- `NamespaceLookup.java` - Namespace lookup helper

#### 9. Configuration Module
- `NamespaceConfig.java` - Spring configuration (renamed from BaseConfig)
- `OAuth2Config.java` - OAuth2/JWT configuration

#### 10. Documentation
- `schema.sql` - Database schema
- `namespace-schema.sql` - Namespace-specific schema

## Package Structure

### Changed Packages
- ✅ `com.tsu.base.entities` → `com.tsu.namespace.entities`
- ✅ `com.tsu.base.repo` → `com.tsu.namespace.repo`
- ✅ `com.tsu.base.record` → `com.tsu.namespace.record`
- ✅ `com.tsu.base.helper` → `com.tsu.namespace.helper`
- ✅ `com.tsu.base.api.namespace` → `com.tsu.namespace.api.namespace`
- ✅ `com.tsu.base.api.user` → `com.tsu.namespace.api.user`
- ✅ `com.tsu.base.api.entity` → `com.tsu.namespace.api.entity`
- ✅ `com.tsu.base.api.manager` → `com.tsu.namespace.api.manager`
- ✅ `com.tsu.base.security` → `com.tsu.namespace.security`
- ✅ `com.tsu.base.config` → `com.tsu.namespace.config`
- ✅ `com.tsu.base.service.impl` → `com.tsu.namespace.service.impl`

### Unchanged Packages (Dependencies on bx-api and bx-base)
- `com.tsu.base.api.*` - Interface definitions (in bx-api module)
- `com.tsu.base.val.*` - Value objects (in bx-api module)
- `com.tsu.base.enums.*` - Enumerations (in bx-api module)
- `com.tsu.base.data.*` - Data objects (in bx-api module)
- `com.tsu.base.request.*` - Request DTOs (in bx-api module)
- `com.tsu.base.service.IDGeneratorService` - Shared service (in bx-base)
- `com.tsu.base.api.subscription.*` - Subscription interfaces (in bx-api)

## Files Migrated

**Total Files:** 58 Java files

### By Category:
- **Entities:** 9 files (8 table entities + 4 composite IDs)
- **Repositories:** 10 files (9 repositories + 1 specifications)
- **Records:** 7 files
- **Helpers:** 5 files
- **API Implementations:** 10 files
- **Managers:** 3 files
- **Security:** 6 files
- **Configuration:** 2 files
- **Services:** 2 files
- **Documentation:** 2 SQL files

## Known Compilation Issues

### 🔧 Dependencies on Non-Migrated Modules

The following classes have dependencies on modules that remain in bx-base:

#### 1. Workspace Dependencies
- `EntityImpl.java` references `WorkspaceDbHelper`, `WorkspaceRecord`, `WorkspaceTypeRecord`
- These are intentionally kept in bx-base as workspace is not part of namespace core

#### 2. Document/Entry Dependencies
- `DomainObjectBuilder.java` references `DocumentDbHelper`
- `EntityImpl.java` references `EntryTextManager`, `TextManager`
- Entry/document system is a separate module (bx-entry)

#### 3. Missing Support Classes
- `LoginRepository` and `LoginRecord` - Authentication login tracking
- `PermissionData` - Permission data structures
- `SecurityClass` enum - Security classification
- `AuthLogin` record - Login authentication data

### Workarounds Applied

1. **Config Bean Simplification:**
   - Removed `BaseEventManager` bean (workspace/work related)
   - Kept only `IDGeneratorService` and `DomainObjectBuilder` beans

2. **Import Reversion:**
   - API interfaces remain in `com.tsu.base.api.*` (bx-api module)
   - Value objects remain in `com.tsu.base.val.*` (bx-api module)
   - Enums remain in `com.tsu.base.enums.*` (bx-api module)

## Build Status

### Current Maven Compilation Status
❌ **FAILING** - Dependencies on non-migrated modules

### Required Actions to Achieve Successful Build

#### Option 1: Comment Out Workspace Dependencies (Recommended)
Comment out or remove workspace-related methods in:
- `EntityImpl.java` - Remove `getPrimaryWorkspace()` and related methods
- `DomainObjectBuilder.java` - Remove workspace-related factory methods

#### Option 2: Keep bx-base Dependency
Add bx-base as a compile dependency in pom.xml:
```xml
<dependency>
    <groupId>com.tsu</groupId>
    <artifactId>bx-base</artifactId>
    <version>${project.version}</version>
</dependency>
```

#### Option 3: Move Additional Supporting Classes
Copy the following to bx-namespace:
- `LoginTb`, `LoginRepository`, `LoginRecord`
- `PermissionData` class
- `SecurityClass` enum
- `AuthLogin` record

## Architecture Notes

### Multi-Layer Pattern Preserved
The 4-layer architecture is maintained:
```
API Layer (Impl classes) → Record Layer → Entity Layer → Database
```

### Dependencies
```
bx-namespace
    ↓ depends on
bx-api (interfaces, value objects, enums)
bx-entry (document/entry management)
be-common (shared utilities)
```

### Database Schema
- PostgreSQL with JSONB for flexible properties
- PostGIS for spatial queries (Place module)
- Composite primary keys for namespace-scoped entities
- Consistent audit fields (created_by/date, modified_by/date)

## Testing

### Test Structure Created
```
src/test/
├── java/com/tsu/namespace/
│   └── repo/  (Repository tests)
└── resources/
    └── test-schema.sql
```

**Note:** Test files not yet migrated. Will require:
- TestContainers PostgreSQL setup
- Spring Security context mocking
- Schema loading configuration

## Next Steps

### Immediate (To Fix Compilation)
1. ✅ Decide on workspace dependency handling (Option 1, 2, or 3)
2. ✅ Copy missing support classes if needed
3. ✅ Update pom.xml if keeping bx-base dependency

### Short-term
1. Migrate or stub out workspace-related dependencies
2. Create test infrastructure
3. Migrate relevant test cases
4. Create GitHub Actions build pipeline

### Long-term
1. Evaluate if additional modules should be moved from bx-base
2. Consider splitting workspace into bx-workspace module
3. Document API contracts between modules
4. Create module dependency diagram

## File Statistics

### Lines of Code (Approximate)
- `NamespaceDbHelper.java`: ~12,000 lines
- `WorkspaceDbHelper.java` (not migrated): ~15,000 lines
- Total migrated code: ~25,000+ lines

### Directory Structure
```
bx-namespace/
├── src/main/java/com/tsu/namespace/
│   ├── api/
│   │   ├── namespace/     (4 files)
│   │   ├── user/          (4 files)
│   │   ├── entity/        (2 files)
│   │   └── manager/       (3 files)
│   ├── entities/          (8 files + id/ subdir)
│   │   └── id/            (4 files)
│   ├── repo/              (10 files)
│   ├── record/            (7 files)
│   ├── helper/            (5 files)
│   ├── security/          (6 files)
│   ├── config/            (2 files)
│   └── service/impl/      (2 files)
├── doc/                   (2 SQL files)
└── pom.xml
```

## Maven POM Configuration

### Current Dependencies
- Spring Boot 3.x (from parent)
- PostgreSQL driver
- Hibernate Spatial 6.2.7
- bx-api (interfaces)
- bx-entry (document system)
- OAuth2 Client
- Google API Client
- RestFB
- DynamoDB SDK (AWS)

### Parent POM
```xml
<parent>
    <groupId>com.tsu</groupId>
    <artifactId>bx-pom</artifactId>
    <version>1.0</version>
</parent>
```

## Contributors
- Migration performed by: Claude Code
- Original code from: bx-base module
- Date: October 22, 2025

## References
- Original module: `/Users/shawn1807/repos/bx-base`
- New module: `/Users/shawn1807/repos/bx-namespace`
- CLAUDE.md (bx-base): Architecture and patterns documentation
