# Smart Healthcare Reminder System

Backend-first implementation of the Tekravio Java Healthcare assignment.

## What This System Does

Patients upload prescriptions, the backend stores them in private AWS S3 storage, extracts text using AWS Textract, creates medicine and diet schedules, and sends reminders through Firebase Cloud Messaging. If FCM is missing or fails, the backend can fall back to WhatsApp through Twilio.

## Current Setup

- AWS S3 bucket: `healthcare-reminder-prescriptions`
- AWS IAM user: `healthcare-reminder-aws-dev`
- Local AWS CLI profile: `healthcare-dev`
- Firebase project: `healthcare-reminder-dev`
- Twilio trial account: created for WhatsApp sandbox testing

## Repository Structure

```text
backend/          Spring Boot API
docs/             Postman collection, diagrams, notes
web/              React dashboard, added after backend baseline
```

## Local Requirements

- Java 21 or newer
- Maven 3.9+
- Docker Desktop
- PostgreSQL through Docker Compose
- AWS CLI profile named `healthcare-dev`

## Quality Gates

GitHub Actions runs the backend test suite and frontend production build on every push to `main` and every pull request.

```text
.github/workflows/backend-ci.yml
```

The pipeline also validates Docker builds for the backend and web dashboard.

## First Run

Start PostgreSQL:

```bash
docker compose up -d
```

Run the backend:

```bash
cd backend
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/api/v1/health
```

Run the frontend:

```bash
cd web
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

## Production-Ready Deployment Baseline

This repository includes Docker packaging for both apps:

```text
backend/Dockerfile
web/Dockerfile
docker-compose.prod.yml
```

Before deploying, set strong production values for:

```text
POSTGRES_PASSWORD
JWT_SECRET
AWS_S3_BUCKET
AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY, or an IAM role
FIREBASE_CREDENTIALS_PATH
TWILIO_ACCOUNT_SID
TWILIO_AUTH_TOKEN
CORS_ALLOWED_ORIGINS
BOOTSTRAP_ADMIN_EMAIL
BOOTSTRAP_ADMIN_PASSWORD
```

Production compose example:

```bash
docker compose -f docker-compose.prod.yml up --build
```

For AWS deployment, prefer IAM roles over static access keys when running on EC2/ECS. Keep S3 private, use HTTPS at the load balancer or reverse proxy, and rotate Twilio/Firebase/AWS secrets outside Git.

Before handling real patient data, complete the [production checklist](docs/production-checklist.md).

## Postman Testing

Import these files into Postman:

```text
docs/postman/healthcare-reminder.postman_collection.json
docs/postman/healthcare-reminder.local.postman_environment.json
```

Simple flow:

```text
1. Register Patient
2. Get My Profile
3. Login Admin
4. Upload Prescription
5. Create Medicine Schedule
6. Create Diet Plan
7. Test reminder actions
```

For a simple live walkthrough, follow [docs/demo-script.md](docs/demo-script.md).

## Implemented APIs

### Authentication

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

Registration creates a `PATIENT` user, patient profile, JWT access token, and refresh token. Logout revokes refresh tokens and blacklists the current access token.

### Patient

```text
GET /api/v1/patients/me
PUT /api/v1/patients/me
PUT /api/v1/patients/me/fcm-token
```

### Admin

```text
GET /api/v1/admin/patients
GET /api/v1/admin/patients/{patientId}
PATCH /api/v1/admin/patients/{patientId}/activate
PATCH /api/v1/admin/patients/{patientId}/deactivate
```

### Prescriptions

```text
POST /api/v1/prescriptions
GET /api/v1/prescriptions
GET /api/v1/prescriptions/{prescriptionId}
PUT /api/v1/prescriptions/{prescriptionId}/medicines
```

Prescription upload accepts JPG, PNG, and PDF files up to 10 MB. Files are stored privately in S3 under `prescriptions/{patientId}/{uuid}.{ext}`. Textract extracts raw text and the backend attempts a first-pass medicine parse. Low-confidence or empty extraction results are marked for manual review, and the manual medicines endpoint lets the patient correct the OCR result.

### Medicine Schedules And Reminders

```text
POST /api/v1/medicine-schedules
GET /api/v1/medicine-schedules
PATCH /api/v1/medicine-schedules/{scheduleId}/deactivate
GET /api/v1/medicine-schedules/reminders
PATCH /api/v1/medicine-schedules/reminders/{reminderId}/taken
PATCH /api/v1/medicine-schedules/reminders/{reminderId}/skipped
PATCH /api/v1/medicine-schedules/reminders/{reminderId}/snooze
GET /api/v1/medicine-schedules/adherence
```

The scheduler polls every five minutes, creates idempotent reminder logs for due medicine time slots, sends reminders through Firebase Cloud Messaging, and supports taken/skipped/snoozed actions.

### Firebase Notifications

Medicine reminders are sent through Firebase Cloud Messaging when `FIREBASE_CREDENTIALS_PATH` points to a Firebase service-account JSON file. Every FCM attempt is written to `notification_log`.

FCM data payload:

```text
patientId
scheduleId
action=TAKE
medicineName
```

If a patient has no FCM token, the attempt is logged as failed. If FCM fails, the backend retries once after `FCM_RETRY_DELAY_MS`, which defaults to 60 seconds.

### WhatsApp Fallback

If Firebase cannot be used for a medicine reminder, the backend tries WhatsApp through Twilio.

Fallback order:

```text
1. Try Firebase FCM
2. If FCM token is missing or FCM fails, try WhatsApp
3. Log every FCM and WhatsApp attempt in notification_log
```

Required Twilio variables:

```bash
TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
```

Patient `whatsappNumber` should be stored in E.164 format, for example `+919876543210`. The backend adds the `whatsapp:` prefix before sending.

### Diet Plans And Reminders

Admin APIs:

```text
POST /api/v1/admin/diet-plans
GET /api/v1/admin/diet-plans/patient/{patientId}
PATCH /api/v1/admin/diet-plans/{dietPlanId}/deactivate
```

Patient APIs:

```text
GET /api/v1/diet/today
GET /api/v1/diet/reminders
PATCH /api/v1/diet/reminders/{reminderId}/eaten
PATCH /api/v1/diet/reminders/{reminderId}/skipped
GET /api/v1/diet/compliance
```

Diet reminders are generated 15 minutes before the scheduled meal time. Patients can mark meals as eaten or skipped, and the backend calculates weekly compliance.

To bootstrap an admin user locally, set:

```bash
BOOTSTRAP_ADMIN_EMAIL=admin@example.com
BOOTSTRAP_ADMIN_PASSWORD=change-me-strong-password
```

## Environment Variables

Copy `.env.example` to `.env` when local secrets are needed.

Never commit real AWS keys, Firebase service account JSON, or Twilio auth tokens.

## Assignment Design Answers

### 1. What if the scheduler runs every 5 minutes and the server restarts mid-run?

The backend stores reminder work in the database with an idempotency key. Very simple meaning: before sending a reminder, the app writes down, "I am sending this exact reminder for this exact patient and time." If the server restarts, the next run checks the database and continues from there instead of blindly sending duplicates.

For a larger production version, the scheduler would also use database row locks or a queue so multiple servers can work safely at the same time.

### 2. What if Textract reads a handwritten prescription wrongly?

The backend saves the raw extracted text and marks weak extraction as manual review. Very simple meaning: the system does not fully trust OCR. It says, "I tried reading it, but a human should confirm this medicine list." The manual correction API lets the patient fix the medicine names before reminders are created.

### 3. What if a Firebase FCM token becomes invalid?

Every notification attempt is logged. If FCM fails, the backend retries once. If it still cannot send, it falls back to WhatsApp when the patient has a WhatsApp number. Very simple meaning: first try app notification, then try WhatsApp as backup.

### 4. How is sensitive patient data protected?

Private prescription storage: prescription files go to private S3 paths and are accessed through signed URLs.

JWT and role checks: patients can only access their own records, and admin APIs require the `ADMIN` role.

Secrets stay outside Git: AWS keys, Firebase JSON, and Twilio tokens are loaded from environment variables and ignored by Git.

### 5. How would this scale from 100 to 10,000 patients?

The backend uses pagination, database indexes, idempotent reminder logs, and a scheduler that creates due reminders instead of sending blindly. For 10,000 patients, the next step would be running multiple scheduler workers with database locks or a queue, batching notification sends, and separating notification processing from the main API.
