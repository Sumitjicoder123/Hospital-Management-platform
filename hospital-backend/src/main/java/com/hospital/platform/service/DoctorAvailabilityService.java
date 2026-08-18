package com.hospital.platform.service;

import com.hospital.platform.dto.OperationDTOs.AvailableSlotResponse;
import com.hospital.platform.dto.OperationDTOs.DoctorAvailabilityRequest;
import com.hospital.platform.entity.Appointment;
import com.hospital.platform.entity.DoctorAvailability;
import com.hospital.platform.repository.AppointmentRepository;
import com.hospital.platform.repository.DoctorAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorAvailabilityService(DoctorAvailabilityRepository availabilityRepository, AppointmentRepository appointmentRepository) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public void saveAvailability(Long doctorId, List<DoctorAvailabilityRequest> requests) {
        availabilityRepository.deleteByDoctorId(doctorId);
        List<DoctorAvailability> availabilities = requests.stream().map(req -> {
            DoctorAvailability avail = new DoctorAvailability();
            avail.setDoctorId(doctorId);
            avail.setDayOfWeek(req.getDayOfWeek());
            avail.setStartTime(req.getStartTime());
            avail.setEndTime(req.getEndTime());
            avail.setSlotDurationMinutes(req.getSlotDurationMinutes() != null ? req.getSlotDurationMinutes() : 15);
            return avail;
        }).collect(Collectors.toList());
        availabilityRepository.saveAll(availabilities);
    }

    public List<DoctorAvailability> getAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    public List<AvailableSlotResponse> getAvailableSlots(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorId(doctorId);
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());

        List<AvailableSlotResponse> slots = new ArrayList<>();
        
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            List<DoctorAvailability> dayAvailabilities = availabilities.stream()
                    .filter(a -> a.getDayOfWeek() == currentDate.getDayOfWeek())
                    .collect(Collectors.toList());

            for (DoctorAvailability avail : dayAvailabilities) {
                LocalDateTime currentSlot = LocalDateTime.of(date, avail.getStartTime());
                LocalDateTime endBoundary = LocalDateTime.of(date, avail.getEndTime());

                while (currentSlot.plusMinutes(avail.getSlotDurationMinutes()).isBefore(endBoundary) || 
                       currentSlot.plusMinutes(avail.getSlotDurationMinutes()).isEqual(endBoundary)) {
                    
                    final LocalDateTime slotStart = currentSlot;
                    final LocalDateTime slotEnd = currentSlot.plusMinutes(avail.getSlotDurationMinutes());
                    
                    // Exclude past slots for today
                    if (slotStart.isAfter(LocalDateTime.now())) {
                        boolean isBooked = existingAppointments.stream().anyMatch(appt -> 
                            (appt.getStatus() == Appointment.AppointmentStatus.SCHEDULED || 
                             appt.getStatus() == Appointment.AppointmentStatus.BOOKED) &&
                            appt.getAppointmentTime().isEqual(slotStart)
                        );
                        slots.add(new AvailableSlotResponse(slotStart, slotEnd, !isBooked));
                    }
                    currentSlot = slotEnd;
                }
            }
        }
        return slots;
    }

    public List<com.hospital.platform.dto.OperationDTOs.AvailabilitySummaryResponse> getAvailabilitySummary(Long doctorId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        
        List<DoctorAvailability> availabilities = getAvailability(doctorId);
        List<com.hospital.platform.dto.OperationDTOs.AvailabilitySummaryResponse> summary = new ArrayList<>();
        
        if (availabilities.isEmpty()) {
            for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
                summary.add(new com.hospital.platform.dto.OperationDTOs.AvailabilitySummaryResponse(date, false));
            }
            return summary;
        }

        // To determine hasAvailability efficiently without generating exact slots, 
        // we can just check if the doctor has ANY availability for that DayOfWeek 
        // and if it's not a past date.
        // For a full system we'd also check if all slots are booked, but for summary 
        // returning true if there are potential slots is sufficient for the calendar.
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            final java.time.DayOfWeek day = date.getDayOfWeek();
            boolean hasSchedule = availabilities.stream().anyMatch(a -> a.getDayOfWeek() == day);
            boolean isPast = date.isBefore(LocalDate.now());
            summary.add(new com.hospital.platform.dto.OperationDTOs.AvailabilitySummaryResponse(date, hasSchedule && !isPast));
        }

        return summary;
    }
}
