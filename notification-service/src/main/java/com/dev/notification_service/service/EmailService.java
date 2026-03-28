package com.dev.notification_service.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendProjectCreateMail(String toEmail, String name){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

            Context context = new Context();
            context.setVariable("projectName",name);
            context.setVariable("email",toEmail);
            String htmlContent = templateEngine.process("project-created",context);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("devFlow vault ready!");
            helper.setText(htmlContent,true);

            mailSender.send(message);
            log.info("Email sent to {}",toEmail);
        }catch(Exception e){
            log.error("error occured during mail sending event to: {}",toEmail);
            throw new RuntimeException("error occurred during mail service" + e.getMessage());
        }
    }
}
