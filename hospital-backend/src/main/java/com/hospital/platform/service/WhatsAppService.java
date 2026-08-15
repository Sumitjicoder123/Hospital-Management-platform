package com.hospital.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin wrapper around the Meta WhatsApp Cloud API (graph.facebook.com).
 *
 * For the hackathon demo, set whatsapp.enabled=false (default) to log outgoing
 * messages to the console instead of calling the real API — useful when you
 * don't yet have a WhatsApp Business/Cloud API sandbox number set up.
 * Flip whatsapp.enabled=true and fill in the access token + phone number id
 * once you've created a free Meta developer app + WhatsApp test number.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final RestTemplate restTemplate;

    @Value("${whatsapp.enabled:false}")
    private boolean enabled;

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.api-version:v20.0}")
    private String apiVersion;

    public WhatsAppService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendTextMessage(String toPhone, String body) {
        if (toPhone == null || toPhone.isBlank()) {
            return;
        }

        if (!enabled || accessToken.isBlank() || phoneNumberId.isBlank()) {
            // Demo-safe fallback: no real WhatsApp credentials configured yet.
            log.info("[WhatsApp SIMULATED] -> {} : {}", toPhone, body);
            return;
        }

        String url = String.format("https://graph.facebook.com/%s/%s/messages", apiVersion, phoneNumberId);

        Map<String, Object> textPayload = new HashMap<>();
        textPayload.put("body", body);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", normalizePhone(toPhone));
        payload.put("type", "text");
        payload.put("text", textPayload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
        } catch (Exception e) {
            // Never let a WhatsApp delivery failure break the underlying booking/queue flow.
            log.warn("Failed to send WhatsApp message to {}: {}", toPhone, e.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        // WhatsApp Cloud API wants digits only, with country code, no '+' or spaces.
        return phone.replaceAll("[^0-9]", "");
    }
}
