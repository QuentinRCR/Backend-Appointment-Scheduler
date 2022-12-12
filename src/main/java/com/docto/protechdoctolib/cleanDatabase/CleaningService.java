package com.docto.protechdoctolib.cleanDatabase;

import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.registration.token.ConfirmationTokenRepository;
import com.docto.protechdoctolib.rendez_vous.Rendez_vous;
import com.docto.protechdoctolib.rendez_vous.Rendez_vousDAO;
import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CleaningService {
    private CleanRepository cleanRepository;

    private UserRepository userRepository;

    private ConfirmationTokenRepository confirmationTokenRepository;

    private Rendez_vousDAO rendez_vousDAO;

    public CleaningService(CleanRepository cleanRepository, UserRepository userRepository, ConfirmationTokenRepository confirmationTokenRepository, Rendez_vousDAO rendez_vousDAO) {
        this.cleanRepository = cleanRepository;
        this.userRepository = userRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.rendez_vousDAO = rendez_vousDAO;
    }

    @Scheduled(cron = "0 15 10 15 * ?")// Tous les 30 jours
    public void cleanDatabase(){
        List<ConfirmationToken> aa = cleanRepository.findCrenauxToDelete(LocalDateTime.now().minus(Duration.ofDays(1825)));
        List<User> usersToDelete = new ArrayList<User>();
        for (int i=0; i< aa.size(); i++){
            usersToDelete.add(aa.get(i).getUser());
            List<Rendez_vous> bb = rendez_vousDAO.findAllByIdUser(aa.get(i).getUser().getId());
            for (int j=0; j<bb.size();j++){
                rendez_vousDAO.deleteById(bb.get(j).getId());
            }
            confirmationTokenRepository.deleteById(aa.get(i).getId());
            userRepository.deleteById(usersToDelete.get(i).getId());
        }

    }
}
