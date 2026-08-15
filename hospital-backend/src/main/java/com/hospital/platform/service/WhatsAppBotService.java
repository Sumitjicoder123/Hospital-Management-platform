package com.hospital.platform.service;

import com.hospital.platform.dto.OperationDTOs.AppointmentBookingRequest;
import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import com.hospital.platform.service.WhatsAppSessionService.Session;
import com.hospital.platform.service.WhatsAppSessionService.Step;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the actual conversation logic for OPD booking over WhatsApp.
 * Reuses the same QueueService used by the web app, so a WhatsApp booking
 * ends up in the exact same queue / dashboard / city view as a web booking.
 */
@Service
public class WhatsAppBotService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM, h:mm a");

    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final QueueService queueService;
    private final MedicalRecordService medicalRecordService;
    private final WhatsAppSessionService sessionService;

    public WhatsAppBotService(HospitalRepository hospitalRepository,
                               DepartmentRepository departmentRepository,
                               DoctorRepository doctorRepository,
                               AppointmentRepository appointmentRepository,
                               QueueEntryRepository queueEntryRepository,
                               QueueService queueService,
                               MedicalRecordService medicalRecordService,
                               WhatsAppSessionService sessionService) {
        this.hospitalRepository = hospitalRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.queueService = queueService;
        this.medicalRecordService = medicalRecordService;
        this.sessionService = sessionService;
    }

    public String handleIncomingMessage(String fromPhone, String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        String lower = text.toLowerCase();
        Session session = sessionService.get(fromPhone);

        if (lower.equals("status")) {
            return handleStatusQuery(fromPhone);
        }
        if (lower.equals("history")) {
            return handleHistoryQuery(fromPhone);
        }
        if (lower.equals("cancel")) {
            sessionService.reset(fromPhone);
            return "Okay, cancelled. Send *book* anytime to start a new appointment.";
        }

        switch (session.step) {
            case AWAITING_HOSPITAL_CHOICE:
                return handleHospitalChoice(session, text);
            case AWAITING_DEPARTMENT_CHOICE:
                return handleDepartmentChoice(session, text);
            case AWAITING_DOCTOR_CHOICE:
                return handleDoctorChoice(session, text, fromPhone);
            case AWAITING_NAME:
                return handleNameAndBook(session, text, fromPhone);
            case IDLE:
            default:
                if (lower.equals("hi") || lower.equals("hello") || lower.equals("book")
                        || lower.equals("appointment") || lower.equals("menu") || lower.equals("start")) {
                    return startBookingFlow(session);
                }
                return "Welcome to CityHealth OPD Assistant \uD83D\uDC4B\n\n"
                        + "Reply with:\n"
                        + "*book* - book an OPD appointment\n"
                        + "*status* - check your current queue position\n"
                        + "*history* - view a summary of your last visit\n"
                        + "*cancel* - cancel the current booking flow";
        }
    }

    private String startBookingFlow(Session session) {
        List<Hospital> hospitals = hospitalRepository.findAll().stream()
                .limit(5)
                .collect(Collectors.toList());

        if (hospitals.isEmpty()) {
            return "No hospitals are currently registered on the platform. Please try again later.";
        }

        session.step = Step.AWAITING_HOSPITAL_CHOICE;
        session.lastListedIds = hospitals.stream().map(Hospital::getId).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Let's book your OPD appointment.\n\nChoose a hospital by replying with its number:\n");
        for (int i = 0; i < hospitals.size(); i++) {
            Hospital h = hospitals.get(i);
            sb.append(i + 1).append(". ").append(h.getName());
            if (h.getAvailableBeds() != null) {
                sb.append(" (").append(h.getAvailableBeds()).append(" beds free)");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String handleHospitalChoice(Session session, String text) {
        Long hospitalId = resolveChoice(session, text);
        if (hospitalId == null) {
            return "Please reply with a valid number from the list, or *cancel* to stop.";
        }

        List<Department> departments = departmentRepository.findByHospitalId(hospitalId);
        if (departments.isEmpty()) {
            session.clear();
            return "That hospital has no departments configured yet. Reply *book* to try another hospital.";
        }

        session.hospitalId = hospitalId;
        session.step = Step.AWAITING_DEPARTMENT_CHOICE;
        session.lastListedIds = departments.stream().map(Department::getId).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Which department do you need?\n");
        for (int i = 0; i < departments.size(); i++) {
            sb.append(i + 1).append(". ").append(departments.get(i).getName()).append("\n");
        }
        return sb.toString().trim();
    }

    private String handleDepartmentChoice(Session session, String text) {
        Long departmentId = resolveChoice(session, text);
        if (departmentId == null) {
            return "Please reply with a valid number from the list, or *cancel* to stop.";
        }

        List<Doctor> doctors = doctorRepository.findByHospitalIdAndDepartmentId(session.hospitalId, departmentId)
                .stream()
                .filter(d -> d.getStatus() == Doctor.DoctorStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (doctors.isEmpty()) {
            session.clear();
            return "No doctors are available in that department right now. Reply *book* to try again.";
        }

        session.departmentId = departmentId;
        session.step = Step.AWAITING_DOCTOR_CHOICE;
        session.lastListedIds = doctors.stream().map(Doctor::getId).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Choose a doctor:\n");
        for (int i = 0; i < doctors.size(); i++) {
            Doctor doc = doctors.get(i);
            long waiting = queueEntryRepository.countWaitingPatientsByDoctor(doc.getId());
            int estWait = queueService.calculateWaitTimeMinutes(doc.getId(), QueueEntry.Priority.NORMAL);
            sb.append(i + 1).append(". ").append(doctorLabel(doc.getName()));
            if (doc.getSpecialization() != null) {
                sb.append(" (").append(doc.getSpecialization()).append(")");
            }
            sb.append(" - ").append(waiting).append(" waiting, ~").append(estWait).append(" min\n");
        }
        return sb.toString().trim();
    }

    private String handleDoctorChoice(Session session, String text, String fromPhone) {
        Long doctorId = resolveChoice(session, text);
        if (doctorId == null) {
            return "Please reply with a valid number from the list, or *cancel* to stop.";
        }
        session.doctorId = doctorId;

        // If we've seen this phone number book before, reuse the patient's name
        // instead of asking again.
        String knownName = appointmentRepository.findByPatientPhone(fromPhone).stream()
                .max(Comparator.comparing(Appointment::getId))
                .map(Appointment::getPatientName)
                .orElse(null);

        if (knownName != null && !knownName.isBlank()) {
            return bookAppointment(session, knownName, fromPhone);
        }

        session.step = Step.AWAITING_NAME;
        return "What's the patient's full name?";
    }

    private String handleNameAndBook(Session session, String text, String fromPhone) {
        if (text.isBlank()) {
            return "Please enter the patient's name.";
        }
        return bookAppointment(session, text, fromPhone);
    }

    private String bookAppointment(Session session, String patientName, String fromPhone) {
        AppointmentBookingRequest req = new AppointmentBookingRequest();
        req.setPatientName(patientName);
        req.setPatientPhone(fromPhone);
        req.setHospitalId(session.hospitalId);
        req.setDepartmentId(session.departmentId);
        req.setDoctorId(session.doctorId);
        req.setPriority(QueueEntry.Priority.NORMAL);
        req.setBookingChannel(Appointment.BookingChannel.WHATSAPP);

        QueueEntry entry;
        try {
            entry = queueService.bookAppointmentAndGenerateToken(req);
        } catch (Exception e) {
            session.clear();
            return "Sorry, something went wrong while booking (" + e.getMessage() + "). Reply *book* to try again.";
        }

        session.clear();

        Doctor doctor = doctorRepository.findById(session.doctorId).orElse(null);
        String doctorName = doctor != null ? doctor.getName() : "your doctor";

        return "Booked! \u2705\n\n"
                + "Token: *" + entry.getTokenNumber() + "*\n"
                + "Doctor: " + doctorLabel(doctorName) + "\n"
                + "Estimated wait: ~" + entry.getEstimatedWaitMinutes() + " min\n\n"
                + "We'll message you as your turn approaches. Reply *status* anytime to check your position.";
    }

    private String handleStatusQuery(String fromPhone) {
        List<QueueEntry> entries = queueEntryRepository.findByPatientPhone(fromPhone);
        QueueEntry latest = entries.stream()
                .filter(e -> e.getStatus() == QueueEntry.QueueStatus.WAITING
                        || e.getStatus() == QueueEntry.QueueStatus.CALLED
                        || e.getStatus() == QueueEntry.QueueStatus.IN_CONSULTATION)
                .max(Comparator.comparing(QueueEntry::getId))
                .orElse(null);

        if (latest == null) {
            return "You don't have an active OPD token right now. Reply *book* to book an appointment.";
        }

        String statusLabel = switch (latest.getStatus()) {
            case WAITING -> "Waiting";
            case CALLED -> "You're being called now!";
            case IN_CONSULTATION -> "In consultation";
            default -> latest.getStatus().name();
        };

        return "Token: *" + latest.getTokenNumber() + "*\n"
                + "Status: " + statusLabel + "\n"
                + "Estimated wait: ~" + latest.getEstimatedWaitMinutes() + " min";
    }

    private String handleHistoryQuery(String fromPhone) {
        List<MedicalRecord> records = medicalRecordService.getHistoryByPhone(fromPhone);
        if (records.isEmpty()) {
            return "No visit history found yet for this number.";
        }

        StringBuilder sb = new StringBuilder("Your recent visits:\n\n");
        records.stream().limit(3).forEach(r -> {
            sb.append("\uD83D\uDCC5 ").append(r.getVisitDate().format(DATE_FMT)).append("\n");
            if (r.getDiagnosis() != null && !r.getDiagnosis().isBlank()) {
                sb.append("Diagnosis: ").append(r.getDiagnosis()).append("\n");
            }
            if (r.getPrescription() != null && !r.getPrescription().isBlank()) {
                sb.append("Prescription: ").append(r.getPrescription()).append("\n");
            }
            sb.append("\n");
        });
        return sb.toString().trim();
    }

    // Seed data already stores names as "Dr. Ananya Roy", but a doctor could
    // just as easily be entered without the prefix - handle both cases.
    private String doctorLabel(String name) {
        if (name == null) return "your doctor";
        return name.trim().toLowerCase().startsWith("dr.") || name.trim().toLowerCase().startsWith("dr ")
                ? name
                : "Dr. " + name;
    }

    private Long resolveChoice(Session session, String text) {
        if (session.lastListedIds == null || session.lastListedIds.isEmpty()) {
            return null;
        }
        try {
            int idx = Integer.parseInt(text.trim());
            if (idx < 1 || idx > session.lastListedIds.size()) {
                return null;
            }
            return session.lastListedIds.get(idx - 1);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
