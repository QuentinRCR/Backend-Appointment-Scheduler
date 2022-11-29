package com.docto.protechdoctolib.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



public class EmailService{
    public static void sendEmail(String to,String subject, String text){
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
                return new PasswordAuthentication("protechdocto@gmail.com", "dttzoavnqecwnvjt");
            }
        };

        session = Session.getInstance(props, auth);

        try {

            InternetAddress[] recipients = new InternetAddress[1];
            recipients[0] = new InternetAddress(to);

            message = new MimeMessage(session);
            message.setFrom(new InternetAddress("protechdocto@gmail.com"));
            message.addRecipients(Message.RecipientType.TO, recipients);
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

            System.out.println("Email sent");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Error");
        }

    }
    public static void main(String[] args){
        sendEmail("paul.villeneuve@etu.emse.fr", "Protech","It works !");
    }
}
