package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.user.VerificationDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequiredArgsConstructor
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String email;
    @SneakyThrows
    @Async
    public void sendVerificationMessage(final VerificationDto verificationDto) {
        var mimeMessage = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, true);
        log.info(email);
        var context = getContext(verificationDto.getCode(), verificationDto.getUsername());
        var htmlContent = templateEngine.process("verificationTemplate.html", context);

        helper.setTo(verificationDto.getEmail());
        helper.setSubject("Ваше Такси - Подтверждение");
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    private Context getContext(String code, String username) {
        var context = new Context();
        context.setVariable("code", code);
        context.setVariable("username", username);
        return context;
    }

}
