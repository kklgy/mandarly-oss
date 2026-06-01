# Mandarly OSS Docs

This directory contains public documentation for the open-source edition.

Private production records, customer materials, cloud account details, credentials, and deployment runbooks are intentionally excluded from this repository.

## Index

- [Database design](database/README.md)
- [Frontend notes](frontend/README.md)
- Public integration examples live in the source tree next to the relevant modules.

## Architecture Snapshot

Mandarly OSS is split into three main applications:

- `server/`: Spring Boot backend with modules for system, infra, and education-domain workflows.
- `admin/`: operations console for admins and platform staff.
- `app/`: public student and teacher web app.

The core education domain includes:

- users and authentication
- teacher profiles and qualification review
- packages, orders, and payment lifecycle
- booking and classroom entry
- teacher settlement and withdrawal review
- referral rewards
- notifications and support

## Public Documentation Rules

- Keep examples local-first and provider-neutral.
- Use placeholders for all cloud IDs, callback secrets, API keys, production domains, and account names.
- Do not commit production deployment notes, customer conversations, or private compliance material.
- When documenting an integration, describe the contract and mock/local behavior first.
