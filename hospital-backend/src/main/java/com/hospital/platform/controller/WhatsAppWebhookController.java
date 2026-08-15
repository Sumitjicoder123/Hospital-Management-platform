package com.hospital.platform.controller;

import com.hospital.platform.service.WhatsAppBotService;
import com.hospital.platform.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Webhook endpoints for the Meta WhatsApp Cloud API.
 *
 * Setup (free, ~15 min):
 *  1. Create a Meta developer app -> add the "WhatsApp" product. Meta gives
 *     you a free test phone number automatically.
 *  2. In application.yml (or env vars) set whatsapp.enabled=true,
 *     whatsapp.access-token, whatsapp.phone-number-id, and whatsapp.verify-token.
 *  3. Expose this backend publicly during the demo (ngrok is fine) and set
 *     the webhook URL in the Meta app dashboard to
 *     https://<your-ngrok-url>/api/whatsapp/webhook, using the same
 *     verify-token value.
 *  4. Message the test number from your phone with "book" to try the flow.
 *
 * If whatsapp.enabled=false (default), WhatsAppService just logs outgoing
 * messages instead of calling the real API, so you can still demo/test the
 * booking logic locally without any Meta setup - see WhatsAppService.
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {

    private final WhatsAppBotService whatsAppBotService;
    private final WhatsAppService whatsAppService;

    @Value("${whatsapp.verify-token:demo-verify-token}")
    private String verifyToken;

    public WhatsAppWebhookController(WhatsAppBotService whatsAppBotService, WhatsAppService whatsAppService) {
        this.whatsAppBotService = whatsAppBotService;
        this.whatsAppService = whatsAppService;
    }

    // Meta calls this once, at setup time, to verify you own the endpoint.
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Verification failed");
    }

    // Meta POSTs every inbound WhatsApp message here.
    @PostMapping("/webhook")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Void> receiveMessage(@RequestBody Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null) return ResponseEntity.ok().build();

            for (Map<String, Object> entry : entries) {
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
                if (changes == null) continue;

                for (Map<String, Object> change : changes) {
                    Map<String, Object> value = (Map<String, Object>) change.get("value");
                    if (value == null) continue;

                    List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                    if (messages == null) continue;

                    for (Map<String, Object> message : messages) {
                        String from = (String) message.get("from");
                        Map<String, Object> textNode = (Map<String, Object>) message.get("text");
                        String body = textNode != null ? (String) textNode.get("body") : "";

                        String reply = whatsAppBotService.handleIncomingMessage(from, body);
                        whatsAppService.sendTextMessage(from, reply);
                    }
                }
            }
        } catch (Exception e) {
            // Never fail the webhook - Meta will retry aggressively on non-2xx responses.
            org.slf4j.LoggerFactory.getLogger(WhatsAppWebhookController.class)
                    .warn("Error processing WhatsApp webhook payload: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // Manual test endpoint - lets you exercise the bot logic via curl/Postman
    // without needing a real WhatsApp number connected yet.
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, String>> simulate(@RequestBody Map<String, String> body) {
        String phone = body.getOrDefault("phone", "919999999999");
        String message = body.getOrDefault("message", "");
        String reply = whatsAppBotService.handleIncomingMessage(phone, message);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
