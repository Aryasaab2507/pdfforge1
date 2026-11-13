# PDFforge backend — Render deployment

This repository contains a Docker-based Render service definition.

## What is included
- Spring Boot application under `backend/pdfforge-backend`
- Complete Sejda 5.1.28-SNAPSHOT source under `sejda-source/`
- `POST /api/pdf/merge`: multipart field `files`, accepts 2–20 PDF files (50 MB per file)
- `GET /api/pdf/health`: public health-check endpoint
- Merge endpoint protected by the `X-PDFForge-API-Key` request header
- Docker build that compiles the vendored Sejda source and PDFforge backend in one Maven reactor

## Deploy
1. In Render, create or sync the PDFforge Blueprint from `render.yaml`.
2. Supply a secret value for `PDFFORGE_API_KEY` in the service environment.
3. Wait for the Docker build and confirm the health check at `/api/pdf/health`.

## API use
Send the secret in the `X-PDFForge-API-Key` header when calling `POST /api/pdf/merge`. Never put this key in browser-visible JavaScript. A same-origin server-side proxy on the frontend should read the key from a server-only environment variable.

## Build status
Compilation and real PDF merge integration tests must be confirmed by the Render Docker build and a test request after deployment.

## Security and privacy
The merge route rejects requests unless a valid API key is configured and supplied. Uploaded files are written to temporary disk files and deleted after the merge response is prepared. Authentication and upload throttling are still MVP-level; keep access restricted until the frontend proxy and additional abuse controls are in place.

## Licensing
The Sejda source is licensed under AGPL-3.0. Preserve the upstream license and attribution notices and review network-use/source-disclosure obligations before production use.
