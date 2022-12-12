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

    @Scheduled(fixedRate = 86400000)// Every 24 hours
    public List<Rendez_vous> rappel(){
        List<Rendez_vous> list_RDV = rendez_vousDAO.findAll();
        System.out.println(list_RDV);
        list_RDV.forEach(rdv -> {
            if((rdv.getDateDebut().isAfter(LocalDateTime.now())) && (rdv.getDateDebut().isBefore(LocalDateTime.now().plus(Duration.ofDays(2))))){
                emailService.sendEmail(userRepository.getReferenceById(rdv.getId()).getEmail(),
                        "Rappel de Rendez-vous avec la psychologue de l'école", "BLALALLA");
            }
        });
        return list_RDV;
    }

}
