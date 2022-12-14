package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.email.EmailService;
import com.docto.protechdoctolib.user.UserRepository;
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
    private UserRepository userRepository;

    private EmailService emailService;
    public Rappel_RDV(Rendez_vousDAO rendez_vousDAO, UserRepository userRepository, EmailService emailService) {
        this.rendez_vousDAO = rendez_vousDAO;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "00 00 12 ? * * ")// Tous les jours à 12h
    public void rappel(){
        List<Rendez_vous> list_RDV = rendez_vousDAO.findRendez_vousAfterDate(LocalDateTime.now());
        //list_RDV.forEach(rdv -> {
        for (int i=0; i< list_RDV.size(); i++){
            if((list_RDV.get(i).getDateDebut().isAfter(LocalDateTime.now().plus(Duration.ofHours(12)))) && (list_RDV.get(i).getDateDebut().isBefore(LocalDateTime.now().plus(Duration.ofHours(36))))){
                emailService.sendEmail((userRepository.findById(list_RDV.get(i).getIdUser())).get().getEmail(),
                        "Rappel de Rendez-vous avec la psychologue de l'école", "BLALALLA");
            }
        };
    }

}
