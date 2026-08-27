# Open Management Platform

Open Management Platform is an enterprise-oriented permission management platform with a separated backend and React administration console.

The backend uses JDK 21, Spring Boot 3.2, Spring Cloud Alibaba, PostgreSQL and Redis. The web console uses React 19, TypeScript, Vite 7 and Ant Design 6.

## Features

- Captcha-based tenant code, username and password login
- RS256 JWT issuing, validation and logout blacklist
- User, department, position, role, menu, permission group and permission management
- User-role and user-position assignment
- Role-permission and role-menu assignment
- Mandatory tenant data isolation through the MyBatis-Plus tenant interceptor
- Transactional provisioning of a tenant administrator, organization, selected menus, permissions and super-admin role
- Password change and administrator password reset
- Login and operation audit logs
- Gateway routing, JWT validation and request ID propagation
- Permission-aware React administration console with grouped role authorization and access-denied states

Menu assignment and permission assignment remain independent. Menus control visible navigation entries, while permissions control data access and operations. Permission groups organize permissions only and have no database relation to menus.

Refresh Tokens, OAuth2/OIDC and Flyway are intentionally not used at this stage.

## Project Structure

```text
open-management-platform/
├── platform-gateway                 # API gateway and JWT validation
├── platform-auth                    # Captcha, login and token issuing
├── platform-system                  # System management services
├── platform-common                  # Shared core, web, security, Redis and database modules
├── platform-web                     # React administration console
├── sql                              # PostgreSQL initialization script
└── pom.xml                          # Maven aggregator
```

## Requirements

- JDK 21
- Maven 3.9+
- Node.js 20.19+
- pnpm 11
- PostgreSQL 16.x
- Redis 5.0.14
- Nacos 2.4.3

## Backend

Initialize PostgreSQL by executing the following scripts in order:

```text
sql/01_init.sql
```

The initialization script creates the bootstrap administrator `platform / admin / Admin@123456` and assigns the `SUPER_ADMIN` role. Change the default password immediately after the first login.

The repository does not provide a shared default key. Every deployment must generate its own RSA key pair. From the project root, run:

```bash
mkdir -p config/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out config/keys/jwt-private.pem
openssl pkey -in config/keys/jwt-private.pem -pubout -out config/keys/jwt-public.pem
```

On Windows PowerShell, create the directory with `New-Item -ItemType Directory -Force config/keys` when needed. Auth reads both keys, while Gateway and System read only the public key. The Nacos templates use `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY`; see [`config/keys/README.md`](./config/keys/README.md) for details. Never commit generated private keys.

Then run:

Each service connects to Nacos for service discovery and required remote configuration. Import the templates under `config/nacos/` into the `PLATFORM_GROUP` group before startup. PostgreSQL, Redis and JWT key secrets remain environment-variable placeholders.

```bash
mvn clean install
mvn -pl platform-auth spring-boot:run
mvn -pl platform-system spring-boot:run
mvn -pl platform-gateway spring-boot:run
```

The default Gateway endpoint is `http://127.0.0.1:9100`.

## Web Console

```bash
cd platform-web
pnpm install
pnpm dev
```

Open `http://127.0.0.1:5173`. During development, `/api` is proxied to the Gateway.

## Verification

```bash
mvn test

cd platform-web
pnpm run typecheck
pnpm run build
```

## Documentation

- [Web console guide](./platform-web/README.md)
- [JWT RSA key guide](./config/keys/README.md)
