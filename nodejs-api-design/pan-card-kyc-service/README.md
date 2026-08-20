# PAN Card KYC Service

A small Express API for uploading a KYC document (e.g. a PAN card image), checking its
verification status, and managing reviewer messages threaded on the document.

## Architecture

```
routes -> middleware (auth, upload) -> controller -> service (authorization + validation) -> repository (storage)
```

The repository (`src/repositories/document.repository.js`) is an in-memory `Map`, kept
behind the same interface a real database layer would expose — swap its internals for
Mongoose/Postgres without touching the service, controller, or routes.

## Auth model

Every route requires a `Authorization: Bearer <jwt>` header. The JWT is expected to carry:

- `sub` — the user id (becomes `req.user.id`)
- `role` — `"user"`, `"reviewer"`, or `"admin"`

`src/middleware/auth.middleware.js` verifies the token and rejects with `401` if it's
missing, malformed, or invalid/expired.

Authorization rules (enforced in `document.service.js`):
- A document's **status** is visible to its owner, or to any `reviewer`/`admin`.
- **Messages** can be added by the document's owner or a `reviewer`/`admin`.
- A message can only be **updated/deleted** by its original author, or an `admin`.

## Endpoints

| # | Method | Path | Purpose |
|---|--------|------|---------|
| 1 | `POST` | `/api/v1/kyc/documents` | Upload a PAN card image (`multipart/form-data`, field `panImage`) |
| 2 | `GET` | `/api/v1/kyc/documents/:id/status` | Check verification status |
| 3 | `POST` | `/api/v1/kyc/documents/:id/messages` | Add a reviewer message (`{ "text": "..." }`) |
| 4 | `DELETE` | `/api/v1/kyc/documents/:id/messages/:messageId` | Delete a message |
| 5 | `PATCH` | `/api/v1/kyc/documents/:id/messages/:messageId` | Update a message (`{ "text": "..." }`) |

Uploads are restricted to JPEG/PNG and capped at `MAX_UPLOAD_MB` (default 5MB).

## Running it

```bash
cd nodejs-api-design/pan-card-kyc-service
npm install
cp .env.example .env   # set a real JWT_SECRET
npm run dev
```

## Example requests

```bash
# 1) Upload
curl -X POST http://localhost:3000/api/v1/kyc/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F "panImage=@/path/to/pan.jpg"

# 2) Status
curl http://localhost:3000/api/v1/kyc/documents/<id>/status \
  -H "Authorization: Bearer $TOKEN"

# 3) Add message
curl -X POST http://localhost:3000/api/v1/kyc/documents/<id>/messages \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"text":"Image is blurry, please re-upload"}'

# 4) Delete message
curl -X DELETE http://localhost:3000/api/v1/kyc/documents/<id>/messages/<messageId> \
  -H "Authorization: Bearer $TOKEN"

# 5) Update message
curl -X PATCH http://localhost:3000/api/v1/kyc/documents/<id>/messages/<messageId> \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"text":"Updated comment"}'
```
