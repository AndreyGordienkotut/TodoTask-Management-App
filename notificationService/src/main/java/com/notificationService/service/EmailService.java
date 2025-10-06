package com.notificationService.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;


@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.sender.email}")
    private String senderEmail;

    @Value("${app.mail.sender.name}")
    private String senderName;

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
            logger.info("Email sent to {} with subject: {}", toEmail, subject);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            logger.error("Failed to send email to {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            logger.info("HTML email sent to {} with subject: {}", toEmail, subject);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            logger.error("Failed to send HTML email to {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
}
