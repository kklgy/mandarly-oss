# Mandarly OSS

Mandarly OSS is a full-stack reference implementation for an online 1v1 language learning platform.

It is derived from the RuoYi-Vue-Pro ecosystem and focuses on the product and maintenance surface that a real education SaaS needs: student booking, teacher onboarding, package purchase, payment callbacks, classroom entry, settlement, referral rewards, multi-language UI, and admin operations.

This repository is a sanitized open-source edition. It intentionally excludes production credentials, private deployment scripts, customer documents, and commercial operating records.

## Repository Layout

| Path | Description |
| --- | --- |
| `server/` | Spring Boot 3 / JDK 17 backend, based on the RuoYi-Vue-Pro module structure |
| `admin/` | Vue 3 + Vben admin console for operations and management workflows |
| `app/` | Vue 3 user-facing web app for students and teachers |
| `docs/` | Public architecture, database, frontend, and changelog notes |
| `scripts/` | Local helper scripts that are safe for open-source development |

## Core Capabilities

- Student and teacher account flows
- Teacher profile, qualification, availability, and income workflows
- Package, order, payment, refund, and referral domain models
- Booking, classroom entry, and classroom webhook integration points
- Admin console for platform operations
- Multi-language frontend with responsive PC and mobile layouts
- Database design notes for the education, payment, support, and notification domains

## Local Development

Copy the environment template and fill local-only values:

```bash
cp .env.example .env.local
```

Backend:

```bash
cd server
mvn -DskipTests install
mvn -pl mandarly-server spring-boot:run
```

Admin:

```bash
cd admin
pnpm install
pnpm dev:antd
```

App:

```bash
cd app
pnpm install
pnpm dev
```

Default local ports:

- Backend: `http://localhost:48080`
- Admin: `http://localhost:5666`
- App: `http://localhost:3001`

## Documentation

- [Docs index](docs/README.md)
- [Database design](docs/database/README.md)
- [Frontend notes](docs/frontend/README.md)

## Open-Source Maintenance Scope

The OSS edition is maintained as a reference implementation and reusable base for education SaaS projects. Production-only assets are not part of this repository. Issues and pull requests should focus on:

- domain model correctness
- maintainability and test coverage
- security-sensitive integration boundaries
- local developer experience
- documentation and reproducible setup

## License

MIT. See [LICENSE](LICENSE).

This project is derived from [RuoYi-Vue-Pro](https://github.com/YunaiV/ruoyi-vue-pro), which is also MIT licensed. Original copyright notices are preserved where applicable.
