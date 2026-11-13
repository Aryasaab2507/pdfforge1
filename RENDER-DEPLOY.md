# PDFforge backend — Render deployment

This repository contains a Docker-based Render service definition.

## What is included
- Spring Boot application under `backend/pdfforge-backend`
- Sejda merge adapter using the source APIs from Sejda 5.1.28-SNAPSHOT
- `POST /api/pdf/merge`: multipart form field `files`, accepts 2–20 PDF files (50 MB per file)
- `GET /api/pdf/health`: health-check endpoint
- Docker build that clones the upstream Sejda source tree, adds the PDFforge backend module to its Maven reactor, and builds the executable jar

## Deploy
1. In Render, choose **New > Blueprint**.
2. Connect this repository and select the branch containing this configuration.
3. Review the service and plan in the Blueprint preview before applying it.
4. Wait for the Docker build and confirm the service health check at `/api/pdf/health`.

## Build status
The code has not been compiled in the ChatGPT execution environment. The first Docker build on Render is the compile verification. If Maven fails, use the Render build log to diagnose and fix the exact error before treating the service as deployed.

## Security and privacy
The initial endpoint does not yet include user authentication or persistent storage. Do not expose it for general public use until access control, request throttling, and production file-handling controls are added. Uploaded files are written to temporary disk locations and deleted after the merge response is prepared.

## Licensing
The Sejda SDK 2.x and later is licensed under AGPL-3.0. This build downloads and builds the upstream source; keep its LICENSE and attribution notices, and review AGPL obligations before making this service available over a network or using it in a closed-source product.
