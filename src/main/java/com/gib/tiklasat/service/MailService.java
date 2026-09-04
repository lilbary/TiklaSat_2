package com.gib.tiklasat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String token) {

        String resetLink = "http://localhost:5173/sifre-sifirla?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TıklaSat - Şifre Sıfırlama Talebi");
        message.setText("Merhaba,\n\nŞifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın.\n" +
                "Bu bağlantı sadece 15 dakika geçerlidir.\n\n" +
                resetLink + "\n\n" +
                "Eğer bu talebi siz yapmadıysanız bu e-postayı dikkate almayın.");

        mailSender.send(message);
    }
}