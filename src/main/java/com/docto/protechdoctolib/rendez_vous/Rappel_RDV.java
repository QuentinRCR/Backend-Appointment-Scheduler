package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.email.EmailService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
public class Rappel_RDV {
    private Rendez_vousDAO rendez_vousDAO;

    private EmailService emailService;
    public Rappel_RDV(Rendez_vousDAO rendez_vousDAO) {
        this.rendez_vousDAO = rendez_vousDAO;
    }

    @Scheduled(fixedRate = 10000)// Every 24 hours
    public List<Rendez_vous> rappel(){
        List<Rendez_vous> list_RDV = rendez_vousDAO.rappel();
        System.out.println(list_RDV);
        list_RDV.forEach(rdv -> {
            if((rdv.getDateDebut().isAfter(LocalDateTime.now())) && (rdv.getDateDebut().isBefore(LocalDateTime.now().plus(Duration.ofDays(1)))){
                emailService.sendEmail();
            }
        });
        return list_RDV;
    }


}
