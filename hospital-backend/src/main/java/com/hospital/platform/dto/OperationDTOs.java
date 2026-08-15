package com.hospital.platform.dto;

import com.hospital.platform.entity.*;
import java.util.List;

public class OperationDTOs {

    public static class AppointmentBookingRequest {
        private Long patientId;
        private String patientName;
        private String patientPhone;
        private Long hospitalId;
        private Long departmentId;
        private Long doctorId;
        private QueueEntry.Priority priority = QueueEntry.Priority.NORMAL;
        private Appointment.BookingChannel bookingChannel = Appointment.BookingChannel.WEB;

        public AppointmentBookingRequest() {}

        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }

        public String getPatientPhone() { return patientPhone; }
        public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

        public Long getHospitalId() { return hospitalId; }
        public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

        public QueueEntry.Priority getPriority() { return priority; }
        public void setPriority(QueueEntry.Priority priority) { this.priority = priority; }

        public Appointment.BookingChannel getBookingChannel() { return bookingChannel; }
        public void setBookingChannel(Appointment.BookingChannel bookingChannel) { this.bookingChannel = bookingChannel; }
    }

    public static class QueueCallRequest {
        private QueueEntry.QueueStatus status;
        public QueueCallRequest() {}
        public QueueEntry.QueueStatus getStatus() { return status; }
        public void setStatus(QueueEntry.QueueStatus status) { this.status = status; }
    }

    public static class BedReserveRequest {
        private Long bedId;
        private Long patientId;
        private String patientName;

        public BedReserveRequest() {}
        public Long getBedId() { return bedId; }
        public void setBedId(Long bedId) { this.bedId = bedId; }
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
    }

    public static class HospitalMatchScoreDTO {
        private Hospital hospital;
        private Double score;
        private Double distanceKm;
        private Integer estimatedTravelMinutes;
        private Long availableRequiredBeds;
        private String tag;

        public HospitalMatchScoreDTO() {}
        public HospitalMatchScoreDTO(Hospital hospital, Double score, Double distanceKm, Integer estimatedTravelMinutes, Long availableRequiredBeds, String tag) {
            this.hospital = hospital;
            this.score = score;
            this.distanceKm = distanceKm;
            this.estimatedTravelMinutes = estimatedTravelMinutes;
            this.availableRequiredBeds = availableRequiredBeds;
            this.tag = tag;
        }

        public Hospital getHospital() { return hospital; }
        public Double getScore() { return score; }
        public Double getDistanceKm() { return distanceKm; }
        public Integer getEstimatedTravelMinutes() { return estimatedTravelMinutes; }
        public Long getAvailableRequiredBeds() { return availableRequiredBeds; }
        public String getTag() { return tag; }
    }

    public static class WhatIfSimulationRequest {
        private Integer additionalIcuPatients = 0;
        private Integer additionalEmergencyPatients = 0;
        private Integer additionalGeneralPatients = 0;

        public WhatIfSimulationRequest() {}

        public Integer getAdditionalIcuPatients() { return additionalIcuPatients; }
        public void setAdditionalIcuPatients(Integer additionalIcuPatients) { this.additionalIcuPatients = additionalIcuPatients; }

        public Integer getAdditionalEmergencyPatients() { return additionalEmergencyPatients; }
        public void setAdditionalEmergencyPatients(Integer additionalEmergencyPatients) { this.additionalEmergencyPatients = additionalEmergencyPatients; }

        public Integer getAdditionalGeneralPatients() { return additionalGeneralPatients; }
        public void setAdditionalGeneralPatients(Integer additionalGeneralPatients) { this.additionalGeneralPatients = additionalGeneralPatients; }
    }

    public static class RedistributionItem {
        private String hospitalName;
        private Long hospitalId;
        private Integer currentAvailable;
        private Integer recommendedAllocation;

        public RedistributionItem() {}
        public RedistributionItem(String hospitalName, Long hospitalId, Integer currentAvailable, Integer recommendedAllocation) {
            this.hospitalName = hospitalName;
            this.hospitalId = hospitalId;
            this.currentAvailable = currentAvailable;
            this.recommendedAllocation = recommendedAllocation;
        }

        public String getHospitalName() { return hospitalName; }
        public Long getHospitalId() { return hospitalId; }
        public Integer getCurrentAvailable() { return currentAvailable; }
        public Integer getRecommendedAllocation() { return recommendedAllocation; }
    }

    public static class WhatIfSimulationResponse {
        private Integer currentCityIcuAvailable;
        private Integer additionalIcuDemand;
        private Integer projectedShortage;
        private String severityLevel;
        private List<RedistributionItem> redistributionPlan;

        public WhatIfSimulationResponse() {}
        public WhatIfSimulationResponse(Integer currentCityIcuAvailable, Integer additionalIcuDemand, Integer projectedShortage, String severityLevel, List<RedistributionItem> redistributionPlan) {
            this.currentCityIcuAvailable = currentCityIcuAvailable;
            this.additionalIcuDemand = additionalIcuDemand;
            this.projectedShortage = projectedShortage;
            this.severityLevel = severityLevel;
            this.redistributionPlan = redistributionPlan;
        }

        public Integer getCurrentCityIcuAvailable() { return currentCityIcuAvailable; }
        public Integer getAdditionalIcuDemand() { return additionalIcuDemand; }
        public Integer getProjectedShortage() { return projectedShortage; }
        public String getSeverityLevel() { return severityLevel; }
        public List<RedistributionItem> getRedistributionPlan() { return redistributionPlan; }
    }

    public static class AiQueryRequest {
        private String prompt;
        public AiQueryRequest() {}
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

    public static class AiQueryResponse {
        private String answer;
        private List<String> dataSourcesUsed;

        public AiQueryResponse() {}
        public AiQueryResponse(String answer, List<String> dataSourcesUsed) {
            this.answer = answer;
            this.dataSourcesUsed = dataSourcesUsed;
        }

        public String getAnswer() { return answer; }
        public List<String> getDataSourcesUsed() { return dataSourcesUsed; }
    }

    public static class MedicalRecordRequest {
        private Long patientId;
        private String patientName;
        private String patientPhone;
        private Long hospitalId;
        private Long doctorId;
        private String doctorName;
        private Long departmentId;
        private Long appointmentId;
        private String symptoms;
        private String diagnosis;
        private String prescription;
        private String notes;
        private String bloodPressure;
        private Double temperatureCelsius;
        private Integer pulseRate;
        private Double weightKg;
        private String followUpDate; // ISO date-time string, optional

        public MedicalRecordRequest() {}

        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }

        public String getPatientPhone() { return patientPhone; }
        public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

        public Long getHospitalId() { return hospitalId; }
        public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

        public String getSymptoms() { return symptoms; }
        public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

        public String getPrescription() { return prescription; }
        public void setPrescription(String prescription) { this.prescription = prescription; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getBloodPressure() { return bloodPressure; }
        public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

        public Double getTemperatureCelsius() { return temperatureCelsius; }
        public void setTemperatureCelsius(Double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }

        public Integer getPulseRate() { return pulseRate; }
        public void setPulseRate(Integer pulseRate) { this.pulseRate = pulseRate; }

        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

        public String getFollowUpDate() { return followUpDate; }
        public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }
    }

    public static class WhatsAppMessageDTO {
        private String fromPhone;
        private String messageBody;

        public WhatsAppMessageDTO() {}
        public WhatsAppMessageDTO(String fromPhone, String messageBody) {
            this.fromPhone = fromPhone;
            this.messageBody = messageBody;
        }

        public String getFromPhone() { return fromPhone; }
        public void setFromPhone(String fromPhone) { this.fromPhone = fromPhone; }

        public String getMessageBody() { return messageBody; }
        public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    }
}
