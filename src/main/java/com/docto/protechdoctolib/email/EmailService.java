package com.docto.protechdoctolib.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {
    private final static Logger LOGGER = LoggerFactory
            .getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async()
     public void sendEmail(String to, String subject, String text){
        Properties props;
        Session session;
        MimeMessage message;

        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("noreply.psy.emse@gmail.com", "jejhmjbvnegpixgi");
            }
        };

        session = Session.getInstance(props, auth);

        try {

            InternetAddress[] recipients = new InternetAddress[1];
            recipients[0] = new InternetAddress(to);

            message = new MimeMessage(session);
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, "utf-8");
            helper.setText(text, true);
            helper.setFrom(new InternetAddress("protechdocto@gmail.com"));
            helper.setSubject(subject);
            message.addRecipients(Message.RecipientType.TO, recipients);

            Transport.send(message);

            System.out.println("Email sent");
        } catch (MessagingException e) {
            LOGGER.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        }

    }
}
