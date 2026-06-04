# Demo Script

This is the simple step-by-step flow to demo the assignment.

## 1. Start The Database

Open Docker Desktop first, then run:

```bash
docker compose up -d
```

Very simple meaning: PostgreSQL is the app's notebook. It stores patients, schedules, prescriptions, reminders, and diet plans.

## 2. Start The Backend

```bash
cd backend
mvn spring-boot:run
```

Very simple meaning: this starts the Java API on:

```text
http://localhost:8080
```

## 3. Start The Frontend

```bash
cd web
npm install
npm run dev
```

Very simple meaning: this starts the React dashboard on:

```text
http://localhost:5173
```

## 4. Patient Demo Flow

1. Register a patient.
2. Login as patient.
3. Upload a prescription.
4. Create a medicine schedule.
5. View reminders.
6. Mark a reminder as taken, skipped, or snoozed.
7. View diet plan and diet compliance.

Very simple meaning: this proves the patient side works.

## 5. Admin Demo Flow

1. Login as admin.
2. View patients.
3. Create a diet plan for a patient.
4. Deactivate a diet plan if needed.

Very simple meaning: this proves the admin side works.

## 6. Postman Demo Flow

Import:

```text
docs/postman/healthcare-reminder.postman_collection.json
docs/postman/healthcare-reminder.local.postman_environment.json
```

Then run:

```text
Health Check
Register Patient
Get My Profile
Login Admin
Create Medicine Schedule
Admin Create Diet Plan
Patient Today Diet
```

Very simple meaning: Postman is the API remote control.

## Note About Today's Test

Docker Desktop was not running, so the live smoke test used a temporary in-memory database.

Very simple meaning: today's test data disappears when the backend stops. For the real project demo, start Docker Desktop first so PostgreSQL stores the data properly.
