# Security Policy

## Reporting

Please report security issues privately through GitHub Security Advisories if available, or by opening a minimal issue that does not include exploit details or secrets.

## Sensitive Data Policy

This repository must not contain:

- API keys, OAuth client secrets, webhook secrets, private keys, or passwords
- production hosts, SSH details, cloud account IDs, or object storage bucket names
- customer contracts, compliance documents, or real user data
- production logs or screenshots containing account information

Use `.env.example` for placeholders and keep real values in your local secret manager.
