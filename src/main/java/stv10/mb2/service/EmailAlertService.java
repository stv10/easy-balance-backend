package stv10.mb2.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stv10.mb2.config.ResendProperties;
import stv10.mb2.model.FixedExpense;
import stv10.mb2.repository.FixedExpenseRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailAlertService {

    private static final Logger log = LoggerFactory.getLogger(EmailAlertService.class);

    private final FixedExpenseRepository fixedExpenseRepository;
    private final ResendProperties resendProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 0 9 * * *") // Todos los días a las 9 AM
    public void checkAndSendAlerts() {
        Optional.ofNullable(resendProperties.getApiKey())
                .filter(s -> !s.isEmpty())
                .ifPresentOrElse(
                        apiKey -> {
                            int todayDay = LocalDate.now().getDayOfMonth();
                            List<FixedExpense> dueToday = fixedExpenseRepository.findByDueDay(todayDay);

                            if (dueToday.isEmpty()) {
                                log.info("No fixed expenses due today (day {}).", todayDay);
                                return;
                            }

                            // Construir el cuerpo del correo en HTML
                            StringBuilder htmlMessage = new StringBuilder();
                            htmlMessage.append("<h2>⚠️ Recordatorio de Vencimientos Hoy</h2>");
                            htmlMessage.append("<p>Los siguientes gastos vencen el día de hoy:</p>");
                            htmlMessage.append("<ul>");
                            for (FixedExpense fe : dueToday) {
                                htmlMessage.append("<li><strong>")
                                        .append(fe.getDescription())
                                        .append("</strong>: $")
                                        .append(fe.getAmount())
                                        .append(" (")
                                        .append(fe.getCategory())
                                        .append(")</li>");
                            }
                            htmlMessage.append("</ul>");

                            sendEmail(apiKey, htmlMessage.toString());
                        },
                        () -> log.warn("Resend API Key is not configured. Skipping email alerts."));
    }

    private void sendEmail(String apiKey, String htmlContent) {
        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> payload = new HashMap<>();
            String from = Optional.ofNullable(resendProperties.getFromEmail())
                    .filter(s -> !s.isEmpty())
                    .orElse("onboarding@resend.dev");

            String to = Optional.ofNullable(resendProperties.getToEmail())
                    .filter(s -> !s.isEmpty())
                    .orElse("test@example.com");

            payload.put("from", from);
            payload.put("to", List.of(to));
            payload.put("subject", "⚠️ Recordatorio de Vencimientos Hoy");
            payload.put("html", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.info("Sending email alert to {} via Resend...", to);
            restTemplate.postForObject(url, request, String.class);
            log.info("Email alert sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send email alert via Resend", e);
        }
    }
}
