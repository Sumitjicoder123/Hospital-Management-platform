package com.hospital.platform.service;

import com.hospital.platform.dto.OperationDTOs.AppointmentBookingRequest;
import com.hospital.platform.entity.*;
import com.hospital.platform.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class QueueService {

    private final AppointmentRepository appointmentRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WhatsAppService whatsAppService;
    private final PatientService patientService;

    public QueueService(AppointmentRepository appointmentRepository,
                        QueueEntryRepository queueEntryRepository,
                        DoctorRepository doctorRepository,
                        DepartmentRepository departmentRepository,
                        HospitalRepository hospitalRepository,
                        SimpMessagingTemplate messagingTemplate,
                        WhatsAppService whatsAppService,
                        PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
        this.messagingTemplate = messagingTemplate;
        this.whatsAppService = whatsAppService;
        this.patientService = patientService;
    }

    @Transactional
    public QueueEntry bookAppointmentAndGenerateToken(AppointmentBookingRequest req) {
        // Prevent booking if patient already has an active token
        List<QueueEntry> existingTokens = queueEntryRepository.findByPatientPhone(req.getPatientPhone());
        boolean hasActiveToken = existingTokens.stream().anyMatch(q -> 
            q.getStatus() == QueueEntry.QueueStatus.WAITING || 
            q.getStatus() == QueueEntry.QueueStatus.CALLED || 
            q.getStatus() == QueueEntry.QueueStatus.IN_CONSULTATION
        );
        if (hasActiveToken) {
            throw new RuntimeException("Patient already has an active appointment. Please complete it before booking another.");
        }

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));

        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found: " + req.getDepartmentId()));

        // Recognize returning patients by (normalized) phone number, so visit
        // history stays linked across bookings even if this is their first
        // time being seen by our system, or they typed their number slightly
        // differently than last time.
        Long resolvedPatientId = req.getPatientId();
        var patient = patientService.findOrCreateByPhone(req.getPatientName(), req.getPatientPhone());
        if (patient != null) {
            resolvedPatientId = patient.getId();
        }

        Appointment appt = new Appointment(
                resolvedPatientId,
                req.getPatientName(),
                req.getPatientPhone(),
                req.getDoctorId(),
                req.getDepartmentId(),
                req.getHospitalId(),
                LocalDateTime.now(),
                req.getBookingChannel()
        );
        appointmentRepository.save(appt);

        // Generate Token Number e.g. CARD-014
        String deptPrefix = dept.getCode() != null ? dept.getCode() : "OPD";
        long currentCount = queueEntryRepository.countWaitingPatientsByDoctor(doctor.getId()) + 1;
        String tokenNumber = String.format("%s-%03d", deptPrefix, currentCount);

        QueueEntry entry = new QueueEntry(
                appt.getId(),
                tokenNumber,
                req.getPatientName(),
                req.getPatientPhone(),
                req.getDoctorId(),
                req.getHospitalId(),
                req.getPriority() != null ? req.getPriority() : QueueEntry.Priority.NORMAL
        );
        entry.setPatientId(resolvedPatientId);

        int estimatedWait = calculateWaitTimeMinutes(doctor.getId(), req.getPriority());
        entry.setEstimatedWaitMinutes(estimatedWait);
        entry.setExpectedConsultTime(LocalDateTime.now().plusMinutes(estimatedWait));

        queueEntryRepository.save(entry);

        // Broadcast real-time WebSocket update
        broadcastQueueUpdate(req.getHospitalId());

        whatsAppService.sendTextMessage(entry.getPatientPhone(),
                "Booked! Token *" + entry.getTokenNumber() + "*, estimated wait ~"
                        + entry.getEstimatedWaitMinutes() + " min. Reply *status* anytime to check your position.");

        return entry;
    }

    @Transactional
    public Appointment scheduleAppointment(com.hospital.platform.dto.OperationDTOs.ScheduledAppointmentRequest req) {
        // Prevent booking if they already have an appointment at this exact slot (basic check)
        List<Appointment> existing = appointmentRepository.findByPatientPhone(req.getPatientPhone());
        boolean hasConflict = existing.stream().anyMatch(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED && a.getAppointmentTime().isEqual(req.getSlotStart()));
        if (hasConflict) {
            throw new RuntimeException("Patient already has a scheduled appointment at this time.");
        }

        Long resolvedPatientId = req.getPatientId();
        var patient = patientService.findOrCreateByPhone(req.getPatientName(), req.getPatientPhone());
        if (patient != null) {
            resolvedPatientId = patient.getId();
        }

        Appointment appt = new Appointment(
                resolvedPatientId,
                req.getPatientName(),
                req.getPatientPhone(),
                req.getDoctorId(),
                req.getDepartmentId(),
                req.getHospitalId(),
                req.getSlotStart(),
                req.getBookingChannel()
        );
        appt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appt);
    }

    @Transactional
    public QueueEntry checkInScheduledAppointment(Long appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        if (appt.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
            throw new RuntimeException("Appointment is not in SCHEDULED state.");
        }

        // Prevent checking in on wrong day (optional strictness, but let's allow today)
        if (!appt.getAppointmentTime().toLocalDate().isEqual(java.time.LocalDate.now())) {
            throw new RuntimeException("Can only check-in on the day of the appointment.");
        }

        appt.setStatus(Appointment.AppointmentStatus.CHECKED_IN);
        appointmentRepository.save(appt);

        Doctor doctor = doctorRepository.findById(appt.getDoctorId()).orElseThrow();
        Department dept = departmentRepository.findById(appt.getDepartmentId()).orElseThrow();

        String deptPrefix = dept.getCode() != null ? dept.getCode() : "OPD";
        long currentCount = queueEntryRepository.countWaitingPatientsByDoctor(doctor.getId()) + 1;
        String tokenNumber = String.format("%s-%03d", deptPrefix, currentCount);

        QueueEntry entry = new QueueEntry(
                appt.getId(),
                tokenNumber,
                appt.getPatientName(),
                appt.getPatientPhone(),
                appt.getDoctorId(),
                appt.getHospitalId(),
                QueueEntry.Priority.NORMAL
        );
        entry.setPatientId(appt.getPatientId());

        int estimatedWait = calculateWaitTimeMinutes(doctor.getId(), QueueEntry.Priority.NORMAL);
        entry.setEstimatedWaitMinutes(estimatedWait);
        entry.setExpectedConsultTime(LocalDateTime.now().plusMinutes(estimatedWait));

        queueEntryRepository.save(entry);
        broadcastQueueUpdate(appt.getHospitalId());

        return entry;
    }

    public List<QueueEntry> getQueueForDoctor(Long doctorId) {
        List<QueueEntry.QueueStatus> activeStatuses = Arrays.asList(
                QueueEntry.QueueStatus.WAITING,
                QueueEntry.QueueStatus.CALLED,
                QueueEntry.QueueStatus.IN_CONSULTATION
        );
        return queueEntryRepository.findByDoctorIdAndStatusIn(doctorId, activeStatuses);
    }

    public List<QueueEntry> getQueueByHospital(Long hospitalId) {
        return queueEntryRepository.findByHospitalId(hospitalId);
    }

    public List<QueueEntry> getQueueForPatient(Long patientId) {
        return queueEntryRepository.findByPatientId(patientId);
    }

    public List<QueueEntry> getQueueByPatientPhone(String phone) {
        String normalized = PatientService.normalizePhone(phone);
        return queueEntryRepository.findByPatientPhone(normalized);
    }

    public List<QueueEntry> getQueueByPatientPhoneAndName(String phone, String name) {
        String normalized = PatientService.normalizePhone(phone);
        return queueEntryRepository.findByPatientPhoneAndPatientName(normalized, name);
    }

    public List<Appointment> getPatientAppointments(String phone) {
        String normalized = PatientService.normalizePhone(phone);
        return appointmentRepository.findByPatientPhone(normalized).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Appointment> getPatientAppointmentsByPhoneAndName(String phone, String name) {
        String normalized = PatientService.normalizePhone(phone);
        return appointmentRepository.findByPatientPhoneAndPatientName(normalized, name).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public QueueEntry updateQueueStatus(Long entryId, QueueEntry.QueueStatus newStatus) {
        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Queue Entry not found: " + entryId));

        entry.setStatus(newStatus);
        queueEntryRepository.save(entry);

        // Recalculate wait times for remaining waiting entries for this doctor
        recalculateDoctorQueue(entry.getDoctorId());

        broadcastQueueUpdate(entry.getHospitalId());
        return entry;
    }

    private void recalculateDoctorQueue(Long doctorId) {
        List<QueueEntry> waiting = queueEntryRepository.findByDoctorIdAndStatusIn(
                doctorId, List.of(QueueEntry.QueueStatus.WAITING)
        );

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        int avgTime = (doctor != null && doctor.getConsultationAvgTimeMinutes() != null) ? doctor.getConsultationAvgTimeMinutes() : 15;

        for (int i = 0; i < waiting.size(); i++) {
            QueueEntry q = waiting.get(i);
            int est = i * avgTime;
            q.setEstimatedWaitMinutes(est);
            q.setExpectedConsultTime(LocalDateTime.now().plusMinutes(est));
            queueEntryRepository.save(q);

            // Nudge the patient who is now next (or about to be) so they can
            // head to the hospital, instead of waiting on a screen refresh.
            if (i == 0) {
                whatsAppService.sendTextMessage(q.getPatientPhone(),
                        "You're next! Token *" + q.getTokenNumber() + "* will be called shortly.");
            } else if (i == 2) {
                whatsAppService.sendTextMessage(q.getPatientPhone(),
                        "Heads up: token *" + q.getTokenNumber() + "* is about " + est + " min away. Good time to head to the hospital.");
            }
        }
    }

    public int calculateWaitTimeMinutes(Long doctorId, QueueEntry.Priority priority) {
        Long waitingAhead = queueEntryRepository.countWaitingPatientsByDoctor(doctorId);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        int avgTime = (doctor != null && doctor.getConsultationAvgTimeMinutes() != null) ? doctor.getConsultationAvgTimeMinutes() : 15;

        double baseWait = waitingAhead * avgTime;
        // Apply priority adjustment factor
        double priorityMultiplier = 1.0;
        if (priority == QueueEntry.Priority.EMERGENCY) priorityMultiplier = 0.1;
        else if (priority == QueueEntry.Priority.CRITICAL) priorityMultiplier = 0.3;
        else if (priority == QueueEntry.Priority.HIGH) priorityMultiplier = 0.7;

        return (int) Math.round(baseWait * priorityMultiplier);
    }

    private void broadcastQueueUpdate(Long hospitalId) {
        messagingTemplate.convertAndSend("/topic/queues/" + hospitalId, getQueueByHospital(hospitalId));
    }
}
