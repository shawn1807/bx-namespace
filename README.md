# bx-namespace

Core namespace management module for Business Essentials platform.

## Overview

The `bx-namespace` module provides fundamental multi-tenancy and identity management capabilities, including namespace management, user management, role-based access control, generic entity management, and location/place tracking with spatial capabilities.

## Migrated from bx-base

This module contains namespace-related functionality migrated from `@bx-base`. See [MIGRATION.md](MIGRATION.md) for detailed migration status.

## Key Features

### 1. Namespace Management
- Multi-tenant namespace isolation
- Unique namespace names and URIs
- Subscription control
- Workspace hierarchies
- Permission-based access control

### 2. User Management
- Global user entities with profiles (JSONB)
- Namespace-specific user memberships
- Role-based permissions
- OAuth2/JWT authentication support
- User preferences and settings

### 3. Identity & Principal Management
- Base principal system (users, groups, service accounts)
- Principal type classification
- Identity caching and lookup

### 4. Role-Based Access Control (RBAC)
- Namespace-scoped roles
- Permission assignments (JSONB)
- Hierarchical permission inheritance
- Security classification levels

### 5. Generic Entity Management
- Flexible entity system with types
- JSONB profiles for extensibility
- Parent-child relationships
- Entity lifecycle management

### 6. Place/Location Management
- Geographic location tracking
- PostGIS spatial queries (radius, bounding box)
- Address and building information
- Latitude/longitude coordinates
- Custom properties (JSONB)

## Architecture

### Multi-Layer Data Pattern

```
API Layer         → Business domain interfaces (Entity, Namespace, Place, etc.)
    ↓
Record Layer      → Persistence wrappers with functional callbacks
    ↓
Entity Layer      → JPA entities mapping to PostgreSQL tables
    ↓
Database Layer    → PostgreSQL with JSONB and PostGIS
```

### Package Structure

```
com.tsu.namespace/
├── api/
│   ├── namespace/    # Namespace implementations & factories
│   ├── user/         # User & role implementations
│   ├── entity/       # Generic entity implementations
│   └── manager/      # Sub-domain managers (permissions, places, entities)
├── entities/         # JPA entities (*Tb suffix)
│   └── id/           # Composite ID classes
├── repo/             # Spring Data JPA repositories
├── record/           # Record wrappers with persistence callbacks
├── helper/           # *DbHelper repository facades
├── security/         # Security context implementations
├── config/           # Spring configuration
└── service/impl/     # Business service implementations
```

## Technology Stack

- **Java:** 21
- **Spring Boot:** 3.x
- **Database:** PostgreSQL 14+ with PostGIS
- **ORM:** Hibernate 6.x with Spatial extension
- **Security:** Spring Security OAuth2 Resource Server + JWT
- **Cloud:** AWS DynamoDB support (optional)
- **Build Tool:** Maven

## Dependencies

### Internal Modules
- `bx-api` - Interface definitions, value objects, enumerations
- `bx-entry` - Document/entry management system
- `be-common` - Shared utilities and value objects
- `bx-pom` - Parent POM with shared dependencies

### Key External Libraries
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter OAuth2 Client
- PostgreSQL Driver
- Hibernate Spatial
- Google API Client
- RestFB (Facebook integration)
- AWS SDK for DynamoDB

## Database Schema

### Key Tables
- `base_principal` - All principals (users, groups, service accounts)
- `user_base` - User details with JSONB profile
- `namespace` - Namespace/tenant with JSONB properties
- `namespace_user` - User membership (composite key: namespace_id, id)
- `namespace_role` - Roles with JSONB permissions (composite key)
- `entity` - Generic domain entities (composite key: namespace_id, id)
- `entity_type` - Entity type definitions
- `place` - Locations with PostGIS geometry

### Key Features
- **JSONB columns** for flexible schema evolution
- **PostGIS geometry** for spatial queries
- **Composite primary keys** for namespace-scoped entities
- **Audit fields** on all entities (created_by/date, modified_by/date)
- **Materialized views** for performance optimization

See `doc/schema.sql` and `doc/namespace-schema.sql` for complete DDL.

## Building

### Prerequisites
- JDK 21+
- Maven 3.8+
- PostgreSQL 14+ with PostGIS extension

### Build Commands

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

## Configuration

Copy `src/main/resources/application-template.properties` to `application.properties` and configure:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bx
spring.datasource.username=postgres
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# OAuth2
oauth2.google.client-id=YOUR_CLIENT_ID
oauth2.google.client-secret=YOUR_CLIENT_SECRET

# JWT
jwt.issuer-uri=https://your-issuer.com
```

## Usage Examples

### Creating a Namespace

```java
@Autowired
private NamespaceDbHelper namespaceDbHelper;

public NamespaceRecord createNamespace(String name, String uri, AppSecurityContext context) {
    return namespaceDbHelper.addNamespace(
        name,
        uri,
        context.getPrincipal().getId(),
        "contact@example.com",
        context
    );
}
```

### Adding a User to Namespace

```java
@Autowired
private UserDbHelper userDbHelper;

public UserRecord registerUser(String displayName, String email, AppSecurityContext context) {
    return userDbHelper.register(
        Text.of(displayName),
        Email.of(email),
        null, // phone
        null, // imageUrl
        null, // firstName
        null, // lastName
        new HashMap<>(), // profile
        new HashMap<>(), // preference
        context
    );
}
```

### Spatial Query for Places

```java
@Autowired
private PlaceRepository placeRepository;

public List<PlaceTb> findNearbyPlaces(double lat, double lng, double radiusMeters) {
    return placeRepository.findPlacesWithinRadius(lat, lng, radiusMeters);
}
```

## Security

### Authentication Flow
1. JWT token validation via OAuth2 Resource Server
2. Token conversion to `AppJwtAuthenticationToken`
3. Security context initialization (`WebAppSecurityContext` or `AdminSecurityContext`)
4. Permission checking via `NamespacePermissionManager`

### Multi-Tenancy
- All operations require `AppSecurityContext`
- Namespace-scoped data isolation
- Subscription validation for user operations

## API Contracts

### Domain Interfaces (from bx-api)
- `Namespace` - Namespace operations
- `NamespaceUser` - User membership
- `NamespaceRole` - Role management
- `UserBase` - User operations
- `Entity` - Generic entity
- `Place` - Location management

### Value Objects (from bx-api)
- `NamespaceVal`, `UserVal`, `EntityVal`, `PlaceVal`
- Used for data transfer between layers

## Testing

### Test Infrastructure (To Be Implemented)
- PostgreSQL TestContainers
- Spring Security context mocking
- Integration tests with real database
- Repository unit tests

```bash
# Run tests
mvn test

# Run integration tests
mvn verify
```

## Contributing

### Code Conventions
1. **Entities:** Use `*Tb.java` suffix (e.g., `NamespaceTb`)
2. **Repositories:** Use `*Repository.java` suffix
3. **Helpers:** Use `*DbHelper.java` suffix for repository facades
4. **Records:** Use `*Record.java` suffix for persistence wrappers
5. **API Implementations:** Use `*Impl.java` suffix
6. **Managers:** Use `*Manager.java` suffix for sub-domain logic

### Naming Patterns
- Package names: `com.tsu.namespace.*`
- Entities: Map to database tables
- Records: Wrap entities with functional persistence
- Helpers: Orchestrate multi-repository operations

## Known Issues

See [MIGRATION.md](MIGRATION.md) for current compilation issues and workarounds.

### Current Limitations
1. ❌ Build requires resolution of workspace dependencies
2. ❌ Test infrastructure not yet migrated
3. ⚠️ Some classes reference non-migrated modules (document, workspace)

## Migration Status

✅ **Successfully Migrated:** 58 Java files
✅ **Modules:** Namespace, User, Role, Entity, Place, Security
⚠️ **Compilation:** Requires dependency resolution

See [MIGRATION.md](MIGRATION.md) for detailed status.

## Documentation

- [MIGRATION.md](MIGRATION.md) - Detailed migration status
- [CLAUDE.md](CLAUDE.md) - Build commands and architecture notes (to be created)
- `doc/schema.sql` - Database schema
- `doc/namespace-schema.sql` - Namespace-specific schema

## Module Relationships

```
┌─────────────┐
│   bx-api    │  (Interfaces, Value Objects, Enums)
└──────┬──────┘
       │ extends
       ↓
┌─────────────┐      ┌─────────────┐
│bx-namespace │ ───→ │  bx-entry   │  (Document System)
└──────┬──────┘      └─────────────┘
       │
       ↓ may use
┌─────────────┐
│   bx-base   │  (Workspace, Work, Other Modules)
└─────────────┘
```

## License

Proprietary - Business Essentials Platform

## Support

For issues or questions, contact the development team.

---

**Version:** 1.0
**Last Updated:** October 22, 2025
**Status:** 🚧 Under Migration
