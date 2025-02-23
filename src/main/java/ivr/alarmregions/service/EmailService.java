package ivr.alarmregions.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailPrague(String sender, String recipient, String message, String minulyStav, String explicit, String department) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject("Krizová hláška Praha - " + explicit + "" + department + " změněna");
            mimeMessageHelper.setText("Ke změně došlo: " + LocalDateTime.now().toString().split("\\.")[0] + "\n" +
                    message + "\n\n\n>>Pokud je uvedeno datum a čas, bude přehráváni ukončeno v tomto čase.\n" +
                    ">>Pokud je uvedeno pouze datum, bude přehráváni ukončeno o půlnoci.\n" +
                    ">>Pokud datum uvedeno není, bude se hláška přehrávat až do jejího vypnutí.\n\n\n \n  Předchozí stav : \n\n " +
                    minulyStav);
            mimeMessage.setText(message, "UTF-8");
            log.info("Sending email to: {}", recipient);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully");
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
        }
    }

    public void sendEmailReg(String sender, String recipient, String message, String appUser, String department) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(recipient);
            mimeMessage.setSubject("Krizová hláška" + department);
            mimeMessageHelper.setText("From: Notifikace - Krizové hlášky" + department + "\nTo: " + recipient +
                    "\nSubject: Krizová hláška " + department + "\n\n" +
                    "Ke změně došlo: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                    "\nZmena krizové hlášky " + department + "\n Změnu provedl: " + appUser);
            mimeMessage.setText(message, "UTF-8");
            log.info("Sending email to: {}", recipient);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully");
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
        }
    }

}
