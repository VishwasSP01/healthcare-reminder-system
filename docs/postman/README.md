# Postman Collection

Import these two files into Postman:

- `healthcare-reminder.postman_collection.json`
- `healthcare-reminder.local.postman_environment.json`

Very simple meaning:

1. The collection is the list of API buttons.
2. The environment is the small memory box where Postman stores values like `accessToken`, `patientId`, and `scheduleId`.
3. First run `Register Patient` or `Login Patient`.
4. Then run `Get My Profile`; it saves `patientId`.
5. Then you can test prescription upload, medicine reminders, and diet reminders.

Before testing date-based APIs, update the `today` environment variable to today's date if needed. The backend only accepts today or future dates for schedule start dates.

Do not paste real AWS keys, Firebase service-account JSON, or Twilio auth tokens into Postman.
