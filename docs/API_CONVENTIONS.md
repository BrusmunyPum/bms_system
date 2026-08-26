# BMS API Conventions

Base URL: `/api/v1`. Use JSON, UTF-8, ISO-8601 dates/times, plural resource names and HTTPS in production.

## Resources and actions

```text
GET    /customers?page=0&size=20&search=
POST   /customers
GET    /customers/{id}
PUT    /customers/{id}
POST   /quotes/{id}/submit
POST   /quotes/{id}/accept
POST   /quotes/{id}/convert-to-sales-invoice
POST   /sales-invoices/{id}/confirm
POST   /sales-invoices/{id}/cancel
POST   /payments
POST   /payments/{id}/void
```

Use action endpoints only for real state transitions. Create/update input uses `dto/request`; responses use `dto/response`. Request bodies are validated before service calls.

## Responses

List responses include `content`, `page`, `size`, `totalElements` and `totalPages`. Errors use one envelope:

```json
{"code":"VALIDATION_ERROR","message":"Please correct the highlighted fields.","fieldErrors":{"customerId":"Customer is required"},"traceId":"..."}
```

Return `201` for create, `200` for read/update/actions, `204` only for an authorised draft deletion, `400` for validation, `401` unauthenticated, `403` unauthorised, `404` missing and `409` for a version/state/duplicate conflict.

## Safety

State-changing requests require CSRF protection. Confirm, cancel, post and void requests also include an idempotency key; retries must not create a second posting. The server calculates totals and authorises every request.
