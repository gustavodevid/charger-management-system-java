package com.dac.chargemanager.business.service;

import com.dac.chargemanager.business.dto.ChargeDTO;
import com.dac.chargemanager.infra.config.EmailConfig;
import com.dac.chargemanager.infra.entity.ChargeStatus;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

/**
 * Service for sending email notifications.
 */
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final EmailConfig config;
    private final Session session;

    public EmailService() {
        this.config = EmailConfig.getInstance();
        this.session = createSession();
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", String.valueOf(config.getPort()));
        props.put("mail.smtp.auth", String.valueOf(config.isSmtpAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttls()));

        if (config.isSmtpAuth()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });
        } else {
            return Session.getInstance(props);
        }
    }

    /**
     * Sends a charge status notification email.
     */
    public void sendChargeStatusNotification(ChargeDTO charge, ChargeStatus oldStatus, ChargeStatus newStatus) {
        if (!config.isConfigured()) {
            logger.warn("Email not configured, skipping notification for charge {}", charge.getId());
            return;
        }

        if (charge.getCustomerEmail() == null || charge.getCustomerEmail().isEmpty()) {
            logger.warn("Customer email not available for charge {}", charge.getId());
            return;
        }

        try {
            String subject = getSubject(newStatus, charge);
            String body = getEmailBody(charge, oldStatus, newStatus);

            sendEmail(charge.getCustomerEmail(), subject, body);
            logger.info("Email notification sent to {} for charge {} - status: {}", 
                    charge.getCustomerEmail(), charge.getId(), newStatus);

        } catch (Exception e) {
            logger.error("Failed to send email notification for charge {}", charge.getId(), e);
        }
    }

    /**
     * Sends a charge created notification email.
     */
    public void sendChargeCreatedNotification(ChargeDTO charge) {
        if (!config.isConfigured()) {
            logger.warn("Email not configured, skipping notification for charge {}", charge.getId());
            return;
        }

        if (charge.getCustomerEmail() == null || charge.getCustomerEmail().isEmpty()) {
            logger.warn("Customer email not available for charge {}", charge.getId());
            return;
        }

        try {
            String subject = "Nova cobrança criada - " + formatCurrency(charge.getValue());
            String body = buildChargeCreatedEmail(charge);

            sendEmail(charge.getCustomerEmail(), subject, body);
            logger.info("Charge created email sent to {} for charge {}", 
                    charge.getCustomerEmail(), charge.getId());

        } catch (Exception e) {
            logger.error("Failed to send charge created email for charge {}", charge.getId(), e);
        }
    }

    private void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(config.getFromAddress(), config.getFromName(), "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(config.getFromAddress()));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    private String getSubject(ChargeStatus status, ChargeDTO charge) {
        String value = formatCurrency(charge.getValue());
        return switch (status) {
            case PENDING -> "Cobrança pendente - " + value;
            case REGISTERED -> "Cobrança registrada - " + value;
            case PAID -> "Pagamento confirmado - " + value;
            case CANCELED -> "Cobrança cancelada - " + value;
        };
    }

    private String getEmailBody(ChargeDTO charge, ChargeStatus oldStatus, ChargeStatus newStatus) {
        return switch (newStatus) {
            case REGISTERED -> buildRegisteredEmail(charge);
            case PAID -> buildPaidEmail(charge);
            case CANCELED -> buildCanceledEmail(charge);
            default -> buildStatusChangeEmail(charge, oldStatus, newStatus);
        };
    }

    private String buildChargeCreatedEmail(ChargeDTO charge) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        sb.append("<h2 style='color: #2c3e50;'>Nova Cobrança Criada</h2>");
        sb.append("<p>Olá ").append(charge.getCustomerName()).append(",</p>");
        sb.append("<p>Uma nova cobrança foi criada para você:</p>");
        
        appendChargeDetails(sb, charge);
        appendPaymentInfo(sb, charge);
        
        sb.append("<p style='color: #7f8c8d; font-size: 12px;'>Esta é uma mensagem automática, não responda.</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildRegisteredEmail(ChargeDTO charge) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        sb.append("<h2 style='color: #27ae60;'>Cobrança Registrada</h2>");
        sb.append("<p>Olá ").append(charge.getCustomerName()).append(",</p>");
        sb.append("<p>Sua cobrança foi registrada e está aguardando pagamento:</p>");
        
        appendChargeDetails(sb, charge);
        appendPaymentInfo(sb, charge);
        
        sb.append("<p style='color: #7f8c8d; font-size: 12px;'>Esta é uma mensagem automática, não responda.</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildPaidEmail(ChargeDTO charge) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        sb.append("<h2 style='color: #27ae60;'>✓ Pagamento Confirmado</h2>");
        sb.append("<p>Olá ").append(charge.getCustomerName()).append(",</p>");
        sb.append("<p>Seu pagamento foi confirmado com sucesso!</p>");
        
        appendChargeDetails(sb, charge);
        
        sb.append("<p style='margin-top: 20px;'>Obrigado pela preferência!</p>");
        sb.append("<p style='color: #7f8c8d; font-size: 12px;'>Esta é uma mensagem automática, não responda.</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildCanceledEmail(ChargeDTO charge) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        sb.append("<h2 style='color: #e74c3c;'>Cobrança Cancelada</h2>");
        sb.append("<p>Olá ").append(charge.getCustomerName()).append(",</p>");
        sb.append("<p>Sua cobrança foi cancelada:</p>");
        
        appendChargeDetails(sb, charge);
        
        sb.append("<p style='color: #7f8c8d; font-size: 12px;'>Esta é uma mensagem automática, não responda.</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildStatusChangeEmail(ChargeDTO charge, ChargeStatus oldStatus, ChargeStatus newStatus) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        sb.append("<h2 style='color: #3498db;'>Atualização de Cobrança</h2>");
        sb.append("<p>Olá ").append(charge.getCustomerName()).append(",</p>");
        sb.append("<p>O status da sua cobrança foi atualizado de <strong>")
                .append(oldStatus.getDescription()).append("</strong> para <strong>")
                .append(newStatus.getDescription()).append("</strong>:</p>");
        
        appendChargeDetails(sb, charge);
        
        sb.append("<p style='color: #7f8c8d; font-size: 12px;'>Esta é uma mensagem automática, não responda.</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void appendChargeDetails(StringBuilder sb, ChargeDTO charge) {
        sb.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
        sb.append("<tr><td style='padding: 10px; border-bottom: 1px solid #ddd;'><strong>Valor:</strong></td>");
        sb.append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(formatCurrency(charge.getValue())).append("</td></tr>");
        sb.append("<tr><td style='padding: 10px; border-bottom: 1px solid #ddd;'><strong>Vencimento:</strong></td>");
        sb.append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(charge.getDueDate().format(DATE_FORMATTER)).append("</td></tr>");
        sb.append("<tr><td style='padding: 10px; border-bottom: 1px solid #ddd;'><strong>Tipo:</strong></td>");
        sb.append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(charge.getBillingType()).append("</td></tr>");
        sb.append("<tr><td style='padding: 10px; border-bottom: 1px solid #ddd;'><strong>Status:</strong></td>");
        sb.append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(charge.getStatus()).append("</td></tr>");
        if (charge.getDescription() != null && !charge.getDescription().isEmpty()) {
            sb.append("<tr><td style='padding: 10px; border-bottom: 1px solid #ddd;'><strong>Descrição:</strong></td>");
            sb.append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(charge.getDescription()).append("</td></tr>");
        }
        sb.append("</table>");
    }

    private void appendPaymentInfo(StringBuilder sb, ChargeDTO charge) {
        if (charge.getPixCode() != null && !charge.getPixCode().isEmpty()) {
            sb.append("<div style='background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
            sb.append("<h3 style='color: #2c3e50; margin-top: 0;'>Código PIX:</h3>");
            sb.append("<code style='word-break: break-all;'>").append(charge.getPixCode()).append("</code>");
            sb.append("</div>");
        }
        if (charge.getBoletoCode() != null && !charge.getBoletoCode().isEmpty()) {
            sb.append("<div style='background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
            sb.append("<h3 style='color: #2c3e50; margin-top: 0;'>Código do Boleto:</h3>");
            sb.append("<code>").append(charge.getBoletoCode()).append("</code>");
            sb.append("</div>");
        }
        if (charge.getInvoiceUrl() != null && !charge.getInvoiceUrl().isEmpty()) {
            sb.append("<div style='margin: 20px 0;'>");
            sb.append("<a href='").append(charge.getInvoiceUrl()).append("' ");
            sb.append("style='background: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>");
            sb.append("Acessar Fatura</a>");
            sb.append("</div>");
        }
    }

    private String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMATTER.format(value);
    }
}
