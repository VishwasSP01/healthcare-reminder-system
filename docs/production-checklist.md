# Production Checklist

This project is deployable as a production baseline, but a real healthcare deployment should complete this checklist before handling real patient data.

## Application

- Use Java 21 in production.
- Run backend with PostgreSQL, not the temporary H2 test database.
- Set `JWT_SECRET` to a long random secret stored outside Git.
- Set `CORS_ALLOWED_ORIGINS` to the real frontend domain only.
- Keep `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` disabled after the first admin is created.
- Serve the frontend over HTTPS.
- Put the backend behind HTTPS through a load balancer or reverse proxy.

## AWS

- Keep the prescription S3 bucket private.
- Use IAM roles on AWS hosting when possible.
- If static AWS keys are used locally, rotate them regularly.
- Enable bucket encryption.
- Enable S3 block public access.
- Restrict IAM permissions to the prescription bucket and Textract actions needed by the app.

## Firebase And Twilio

- Store Firebase service-account JSON outside Git.
- Store Twilio credentials outside Git.
- Rotate Twilio auth tokens if they are ever exposed.
- Use Twilio Sandbox only for demos; production WhatsApp requires an approved sender.

## Database

- Use managed PostgreSQL or a backed-up PostgreSQL server.
- Enable automated backups.
- Keep Flyway migrations as the only schema-change path.
- Monitor slow queries and add indexes as traffic grows.

## Operations

- Run CI before every deploy.
- Add centralized logs for backend errors and notification failures.
- Add uptime monitoring for `/actuator/health`.
- Add alerts for failed reminder delivery spikes.
- Use separate environments for local, staging, and production.

## Security And Compliance

- Do not store real patient data in local development.
- Review compliance requirements before real medical use.
- Add audit logging for sensitive patient-data access.
- Add rate limiting for auth endpoints.
- Add account lockout or abuse detection for repeated failed logins.
- Review data retention rules for prescriptions and notification logs.

## Current Status

Ready for:

- Assignment submission
- Local demo
- GitHub review
- Docker-based staging deployment

Not yet enough for:

- Real hospital/patient production use
- Compliance-regulated PHI handling without further security, legal, and operational review
