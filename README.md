# ESTATEPRO

**Discover Spaces. Build Futures.**

EstatePro is a Java full-stack real-estate marketplace and property-management portfolio application. The Spring Boot service serves the responsive vanilla-JavaScript website and JSON API from one origin. It includes a real MySQL profile and an embedded H2 profile for quick local evaluation.

## What works today

- Responsive, locally hosted property collection with server-side keyword, city, property type, sale/rent, budget, bedroom, sorting and pagination filters.
- Public advisor directory and a validated, database-backed contact form.
- Local property photography, Indian locations and INR pricing.
- BCrypt registration/login, expiring signed JWTs, account roles, protected API routes and owner/agent checks.
- ADMIN moderation and user access management; AGENT lead, appointment and booking workflows; SELLER listing submission and management; BUYER favourites, recommendations, enquiries, visits, bookings and demo payments.
- Database-backed dashboards and a downloadable property CSV report.
- Visit-slot validation and a database uniqueness constraint to prevent concurrent agent/time-slot bookings.
- OpenAPI UI at `/swagger-ui.html`.

The application intentionally distinguishes implemented workflows from the larger requested roadmap. In particular, production photo/document uploads, password recovery and email verification, SMTP/in-app notification delivery, reviews, complaints, audit-log UI, full CRM notes/follow-up history, refunds and true monthly analytics are not implemented yet. See [DEVELOPMENT_PROGRESS.md](./DEVELOPMENT_PROGRESS.md) for a transparent checklist. The listing form selects a local curated image; it does not upload files.

## Technology

- Java 21 source target, Spring Boot 3.4, Spring Web, Security, Data JPA, Bean Validation
- Maven, MySQL Connector/J, Hibernate, H2 local profile
- JJWT, BCrypt, OpenAPI / Swagger UI
- HTML, CSS, ES modules and Fetch API
- JUnit 5, Mockito, Spring Security Test

## Architecture

```text
src/main/java/com/estatepro/
  config/       Security, local sample data
  controller/   REST controllers
  dto/          Validated request and response records
  entity/       JPA entities and role/status enums
  exception/    Consistent API error responses
  repository/   Spring Data repositories
  security/     JWT creation and request authentication
  service/      Marketplace and workflow rules
src/main/resources/static/
  index.html, css/, js/, assets/
src/main/resources/
  application*.properties
database/       MySQL schema reference and sample inserts
```

Controllers accept/return DTOs; business and ownership rules live in services. Property search uses database specifications and paging. The production-style MySQL profile requires credentials from environment variables; never commit `.env` or real credentials.

## Quick start (Windows PowerShell)

Requirements: Java 21+ (newer JDKs are also supported for local compilation). The included Maven wrapper downloads Maven on first use. A MySQL 8 instance is only required for the `mysql` profile.

1. Copy `.env.example` to `.env` if you want to keep a local configuration record. Spring does not read `.env` automatically: set the corresponding environment variables in the terminal or VS Code launch configuration.
2. With Maven installed, from the project root:

   ```powershell
   $env:SPRING_PROFILES_ACTIVE = "local"
   # Optional: creates buyer@estatepro.local, seller@estatepro.local,
   # agent@estatepro.local and admin@estatepro.local with this password.
   $env:DEMO_PASSWORD = "choose-a-local-password"
   .\mvnw.cmd spring-boot:run
   ```

3. Open [http://localhost:8080](http://localhost:8080). The site and API share the same port. Local sample listings are seeded automatically. Demo accounts are only created when `DEMO_PASSWORD` is explicitly set; there is no hardcoded administrator password.

To package and test:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/estatepro-1.0.0.jar
```

The target bytecode is Java 21. A newer JDK can compile it; use a Java 21 JDK if your deployment requires the exact supported runtime.

### MySQL profile

Create the database/user (or use a suitably privileged local development account):

```sql
CREATE DATABASE estatepro CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'estatepro'@'localhost' IDENTIFIED BY 'use-a-local-secret';
GRANT ALL PRIVILEGES ON estatepro.* TO 'estatepro'@'localhost';
```

Then set the MySQL profile and credentials in the same PowerShell session:

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_URL = "jdbc:mysql://localhost:3306/estatepro?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "estatepro"
$env:DB_PASSWORD = "use-a-local-secret"
$env:JWT_SECRET = "replace-with-a-random-secret-at-least-32-characters"
$env:DEMO_PASSWORD = "choose-a-local-password"
mvn spring-boot:run
```

Hibernate creates/updates the mapped tables (`spring.jpa.hibernate.ddl-auto=update`). Review schema changes and use a migration tool such as Flyway before deploying to production. [`database/schema.sql`](./database/schema.sql) documents the current relational shape; [`database/sample-data.sql`](./database/sample-data.sql) provides optional MySQL development listings. The local H2 database is stored under `data/estatepro`.

### VS Code

Open the workspace folder and use the `EstatePro (local H2)` launch configuration in `.vscode/launch.json`, or run `mvn spring-boot:run` in the integrated terminal. For a separate MySQL launch configuration, set the MySQL environment variables above first. The frontend is served by Spring Boot; no Node frontend server is required.

## Development accounts

Setting `DEMO_PASSWORD` creates four local-only identities on startup:

| Role | Email |
|---|---|
| Admin | `admin@estatepro.local` |
| Agent | `agent@estatepro.local` |
| Seller | `seller@estatepro.local` |
| Buyer / tenant | `buyer@estatepro.local` |

Use the password you set in `DEMO_PASSWORD`; it is BCrypt-hashed. Leave the variable unset outside an isolated local demo. Public sign-up permits only BUYER and SELLER roles. Agents and administrators must be provisioned by an administrator or through the controlled demo bootstrap.

## API reference

The interactive OpenAPI specification is served at `/swagger-ui.html`; a route summary and example payloads are in [API_DOCUMENTATION.md](./API_DOCUMENTATION.md). Authenticated calls use `Authorization: Bearer <access-token>`. Tokens expire after the configurable `JWT_EXPIRATION_MS` period.

## Images and attribution

Property and editorial images are downloaded and served locally from `src/main/resources/static/assets/images/properties/`. They are resized to a 1200 px width and JPEG quality 82. See [ASSET_CREDITS.md](./ASSET_CREDITS.md) for source IDs and license links. Confirm the applicable source terms before redistributing this portfolio.

## Testing

Run `mvn test`. Current tests cover public listing access, JWT authentication/role access, registration constraints and recommendation scoring; workflow coverage is being expanded. See [TESTING.md](./TESTING.md) for the verification matrix and environment-dependent checks.

## Screenshots

No screenshots have been committed yet. Run the application and capture the public collection and each configured demo-role dashboard before using this section in a portfolio presentation.

## Security and production notes

- Set `JWT_SECRET` to a cryptographically random secret of at least 32 bytes and set a short-lived access-token lifetime appropriate for your deployment.
- Use HTTPS, restrictive CORS, managed secrets, production database credentials and database migrations.
- Local demo account creation is disabled unless `DEMO_PASSWORD` is set.
- The payment endpoint is a **DEMO simulation**; it never accepts card data or transfers money.
- This repository is a portfolio foundation, not a security-certified or production-audited service.
