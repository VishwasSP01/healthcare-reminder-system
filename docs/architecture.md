# Architecture

```text
React / Flutter client
        |
        v
Spring Boot REST API
  |-- Auth and RBAC
  |-- Patient Management
  |-- Prescription Upload
  |-- Reminder Engine
  |-- Diet Module
  |-- Notification Module
  |-- Audit Logging
        |
        +-- PostgreSQL
        +-- AWS S3
        +-- AWS Textract
        +-- Firebase FCM
        +-- Twilio WhatsApp fallback
```

The first version is a modular monolith. This keeps the assignment reliable and testable within the three-day window while leaving clear boundaries for future service extraction.

