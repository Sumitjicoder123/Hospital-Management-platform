package com.hospital.platform.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small in-memory conversation state machine, keyed by WhatsApp phone
 * number. Good enough for a single-instance hackathon demo. If you deploy
 * with more than one backend instance, swap this for a Redis-backed store.
 */
@Service
public class WhatsAppSessionService {

    public enum Step {
        IDLE,
        AWAITING_HOSPITAL_CHOICE,
        AWAITING_DEPARTMENT_CHOICE,
        AWAITING_DOCTOR_CHOICE,
        AWAITING_NAME
    }

    public static class Session {
        public Step step = Step.IDLE;
        public Long hospitalId;
        public Long departmentId;
        public Long doctorId;
        public List<Long> lastListedIds; // ids shown in the last numbered menu, for resolving "2" -> id

        public void clear() {
            step = Step.IDLE;
            hospitalId = null;
            departmentId = null;
            doctorId = null;
            lastListedIds = null;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session get(String phone) {
        return sessions.computeIfAbsent(phone, p -> new Session());
    }

    public void reset(String phone) {
        sessions.put(phone, new Session());
    }
}
