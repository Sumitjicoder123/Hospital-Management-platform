package com.hospital.platform.controller;

import com.hospital.platform.dto.OperationDTOs.*;
import com.hospital.platform.entity.QueueEntry;
import com.hospital.platform.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.hospital.platform.entity.User;
import com.hospital.platform.entity.Role;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OpdQueueController {

    private final QueueService queueService;

    public OpdQueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/appointments")
    public ResponseEntity<QueueEntry> bookAppointment(@RequestBody AppointmentBookingRequest request) {
        return ResponseEntity.ok(queueService.bookAppointmentAndGenerateToken(request));
    }

    @PostMapping("/appointments/schedule")
    public ResponseEntity<com.hospital.platform.entity.Appointment> scheduleAppointment(@RequestBody ScheduledAppointmentRequest request) {
        return ResponseEntity.ok(queueService.scheduleAppointment(request));
    }

    @PostMapping("/appointments/{id}/check-in")
    public ResponseEntity<QueueEntry> checkInScheduledAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.checkInScheduledAppointment(id));
    }

    @GetMapping("/appointments/patient")
    public ResponseEntity<List<com.hospital.platform.entity.Appointment>> getPatientAppointments(@RequestParam String phone, @RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(queueService.getPatientAppointmentsByPhoneAndName(phone, name));
        }
        return ResponseEntity.ok(queueService.getPatientAppointments(phone));
    }

    @GetMapping("/queues/doctor/{doctorId}")
    public ResponseEntity<List<QueueEntry>> getDoctorQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.getQueueForDoctor(doctorId));
    }

    @GetMapping("/queues/hospital/{hospitalId}")
    public ResponseEntity<List<QueueEntry>> getHospitalQueue(@PathVariable Long hospitalId, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            if (user.getRole() == Role.DOCTOR && user.getDoctorId() != null) {
                return ResponseEntity.ok(queueService.getQueueForDoctor(user.getDoctorId()));
            }
        }
        return ResponseEntity.ok(queueService.getQueueByHospital(hospitalId));
    }

    @GetMapping("/queues/patient")
    public ResponseEntity<List<QueueEntry>> getPatientQueue(@RequestParam String phone, @RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(queueService.getQueueByPatientPhoneAndName(phone, name));
        }
        return ResponseEntity.ok(queueService.getQueueByPatientPhone(phone));
    }

    @PutMapping("/queues/{id}/status")
    public ResponseEntity<QueueEntry> updateQueueStatus(@PathVariable Long id, @RequestBody QueueCallRequest request) {
        return ResponseEntity.ok(queueService.updateQueueStatus(id, request.getStatus()));
    }
}
