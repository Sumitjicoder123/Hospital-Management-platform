# What was added

## 1. Patient Medical History
New files:
- `entity/MedicalRecord.java` — diagnosis, prescription, symptoms, vitals, visit date, linked to patientId/phone + hospital/doctor/appointment
- `repository/MedicalRecordRepository.java`
- `service/MedicalRecordService.java`
- `controller/MedicalRecordController.java`

Endpoints:
- `POST /api/medical-records` — doctor saves diagnosis/prescription/vitals when finishing a consultation
- `GET /api/medical-records/patient/{patientId}` — full history for registered patients (doctor "snapshot" panel)
- `GET /api/medical-records/phone/{phone}` — fallback for walk-in/WhatsApp-only patients with no account
- `GET /api/medical-records/hospital/{hospitalId}`
- `GET /api/medical-records/{id}`

## 2. WhatsApp Appointment Booking
New files:
- `service/WhatsAppService.java` — sends messages via Meta WhatsApp Cloud API. Logs to console instead of calling the real API when `whatsapp.enabled=false` (default), so the bot logic works with zero WhatsApp setup during development.
- `service/WhatsAppSessionService.java` — tiny in-memory conversation state machine keyed by phone number
- `service/WhatsAppBotService.java` — the actual booking flow (hospital → department → doctor → name → book), plus `status` and `history` commands. Reuses your existing `QueueService.bookAppointmentAndGenerateToken`, so a WhatsApp booking lands in the exact same queue/dashboard as a web booking.
- `controller/WhatsAppWebhookController.java` — Meta webhook verification + inbound message handling, plus a `POST /api/whatsapp/simulate` endpoint to test the conversation without a real WhatsApp number
- `config/RestTemplateConfig.java`

Modified:
- `service/QueueService.java` — now sends a WhatsApp confirmation on booking, and proactively nudges the patient who's next in line as the queue advances
- `config/SecurityConfig.java` — permit `/api/medical-records/**` and `/api/whatsapp/**`
- `resources/application.yml` — added `whatsapp.*` config block
- `dto/OperationDTOs.java` — added `MedicalRecordRequest`
- `hospital-frontend/src/app/services/api.service.ts` — added matching Angular methods for the new endpoints

## Getting WhatsApp actually working for the demo
1. Create a free Meta developer app → add the "WhatsApp" product (gives you a free test number).
2. Set in `application.yml` (or env vars): `whatsapp.enabled=true`, `whatsapp.access-token`, `whatsapp.phone-number-id`, `whatsapp.verify-token`.
3. Expose your backend publicly during the demo (ngrok works fine) and set that URL + verify-token as the webhook in the Meta dashboard.
4. Message your test number with "book" from your own phone.

Until you do that setup, everything still works — `WhatsAppService` just logs what it would have sent, and you can drive the whole conversation via `POST /api/whatsapp/simulate` with `{"phone": "919999999999", "message": "book"}`, which is genuinely useful for demoing on stage without depending on live internet/WhatsApp during judging.

## 3. Doctor dashboard now scoped to their own patients (bug fix)
Previously, a doctor's dashboard showed the **entire hospital's queue** (every department, every doctor) because `User` accounts had no link to a specific `Doctor` record, and the frontend just called `getHospitalQueue()` regardless of role.

Fixed by:
- `entity/User.java` — added a `doctorId` field linking a DOCTOR-role login to their actual `Doctor` record
- `dto/AuthDTOs.java` — `AuthResponse` and `RegisterRequest` now carry `doctorId`
- `service/AuthService.java` — passes `doctorId` through login/register/getCurrentUser
- `config/DataInitializer.java` — each seeded `doctor.hN@citypulse.in` account is now linked to the first `Doctor` record at that hospital
- `hospital-frontend/.../login.ts` — stores `doctorId` in `localStorage.auth_user` on login/register
- `hospital-frontend/.../app.dashboard.ts` — added `refreshQueue()`, which calls `GET /api/queues/doctor/{doctorId}` (already existed, just wasn't used) instead of the hospital-wide queue when `userRole === 'DOCTOR'`

**Note:** if you register a *new* doctor account through the UI (not the seeded ones), it won't have a `doctorId` yet since there's no UI flow to create/link a `Doctor` record during registration. For the demo, use the seeded `doctor.h1@citypulse.in` .. `doctor.h8@citypulse.in` accounts, which are pre-linked.

## 4. New frontend UI
- hospital-management-platform/hospital-frontend/src/app/app.dashboard.ts` / `app.html` / `app.css`:
  - **Patient History modal** — "History" button on every queue row (doctor view) opens a modal showing all past visits (diagnosis, prescription, vitals) for that patient, looked up by phone number
  - **Complete Consultation modal** — clicking "Complete" now opens a form to record symptoms/diagnosis/prescription/vitals before marking the queue entry COMPLETED, so a visit record always gets created
  - **WhatsApp Bot Demo console** — a chat-style widget (button in the header, all roles) that lets you type messages and see the bot's replies live, calling `POST /api/whatsapp/simulate` under the hood — no real WhatsApp account needed for the demo

## 5. Patient table + history bug fix
Root cause of the "history disappears on repeat visits" issue: there was no actual `Patient` record anywhere — every lookup relied on matching the raw phone-number string across separate bookings, which silently broke if the number was typed with different spacing/prefix each time (e.g. `9876543210` vs `+91 98765 43210`).

Fixed by:
- `entity/Patient.java`, `repository/PatientRepository.java`, `service/PatientService.java`, `controller/PatientController.java` — a real `patients` table, keyed by a **normalized** phone number (digits only), so formatting differences no longer break recognition
- `service/QueueService.bookAppointmentAndGenerateToken` — every booking (web or WhatsApp) now resolves/creates a `Patient` and stamps a real `patientId` onto both the `Appointment` and the `QueueEntry` (previously `QueueEntry` had no `patientId` field at all — added one)
- `service/MedicalRecordService.createRecord` — resolves the same `patientId` as a safety net if the caller didn't already have it
- New endpoints: `GET /api/patients/{id}`, `GET /api/patients/phone/{phone}`

No frontend change was needed for the History button — it already preferred `item.patientId` when present, so it automatically benefits from the backend now populating it correctly.

## 6. ICU Transfer button — fixed
Three real bugs, not one:
- `patientId` was **hardcoded to `88`** in the frontend (leftover demo stub) — now uses the real `patientId` from the queue entry (see #5 above)
- `patientName` wasn't URL-encoded when building the transfer request — a name with a space or special character silently broke the request URL. Fixed in `api.service.ts` (`encodeURIComponent` on all string params)
- When zero hospitals had available beds, the modal just sat there doing nothing with no explanation — now shows an explicit "No hospitals with available ICU beds found" state, and the Dispatch button is disabled until a destination is actually selected
- `TransferController`/`AdmissionController` — `patientId` is now `@RequestParam(required = false)` so a transfer/admission still works even for older records that predate the `Patient` table

## 7. Bed unavailability now auto-triggers city-wide search
This directly answers your question: **previously, no** — if all beds of a type were occupied, an admission request just silently sat as `REQUESTED` with nothing assigned, and there was no UI to even create an admission request in the first place. Now there is:
- New **"Admit"** button next to Complete/Transfer on the doctor's queue row, opening a small modal to pick the required bed type and call `POST /api/admissions/request`
- If a bed is available → reserved immediately, doctor gets a confirmation toast
- If **no bed is available** → the doctor automatically sees the same ranked city-wide hospital search used for ICU transfers (distance, ETA, available beds), with one click to dispatch a transfer — this is the "Golden Flow" from your PRD, now actually wired end-to-end instead of just described
- `triggerTransferModal` was generalized to accept any bed type (was hardcoded to `'ICU'`), so this same flow works for GENERAL, HDU, EMERGENCY, etc., not just ICU


- Patient-facing "my history" view (patients can't yet see their own past visits from the Patient Portal panel — only doctors can, via the queue table)
- No UI flow to link a newly registered doctor account to a Doctor record (see note above)
- `MedicalRecordRequest.followUpDate` expects an ISO-8601 string (e.g. `2026-08-20T10:00:00`); invalid strings are silently ignored rather than failing the save.
