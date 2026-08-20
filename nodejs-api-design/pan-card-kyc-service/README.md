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

## In-memory data model

Everything lives in one `Map`, keyed by document id. A `Document` owns an array of
`Message`s — that's the whole shape:

```
documents: Map<docId, Document>

Document
├── id, ownerId, imagePath, mimeType, size
├── status      "PENDING" | "VERIFIED" | "REJECTED"
├── messages[]  ── Message { id, authorId, text, createdAt, updatedAt }
├── createdAt
└── updatedAt
```

Walking through the 5 APIs in the order they'd actually be called shows how each one
touches this structure:

**1. `POST /` (upload) — creates the Document.** The only endpoint that adds a new key
to the Map. Every other endpoint just reads or mutates what this one created — nothing
below works until a document exists.

```text
documents = Map {}
   ⇩ upload
documents = Map { "doc-1" → { ownerId:"u1", status:"PENDING", messages: [] } }
```

**2. `GET /:id/status` — reads the Document.** `documents.get("doc-1")`, no mutation;
returns `{ id, status, updatedAt }` straight off the object.

**3. `POST /:id/messages` — appends to `messages[]`.**

```text
documents.get("doc-1").messages.push({ id:"msg-1", authorId:"u1", text, ... })

documents = Map { "doc-1" → { ..., messages: [ {id:"msg-1", text:"..."} ] } }
```

**4. `PATCH /:id/messages/:messageId` — edits one array element in place.**

```text
msg = documents.get("doc-1").messages.find(m => m.id === "msg-1")
msg.text = newText; msg.updatedAt = now
```

Only that message changes — the Document and every other message are untouched.

**5. `DELETE /:id/messages/:messageId` — removes one array element.**

```text
doc.messages = doc.messages.filter(m => m.id !== "msg-1")
```

The Document itself stays in the Map; only its `messages[]` shrinks by one.

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
