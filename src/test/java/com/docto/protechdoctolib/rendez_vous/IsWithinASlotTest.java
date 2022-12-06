package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.creneaux.CreneauxDAO;
import com.docto.protechdoctolib.creneaux.CreneauxDTO;
import com.docto.protechdoctolib.email.EmailService;
import com.docto.protechdoctolib.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Permet de tester que la fonction IsWithinASlot fonctionne correctement 
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
public class IsWithinASlotTest {

    @Autowired
    Rendez_vousDAO rendez_vousDAO;
    @Autowired
    CreneauxDAO creneauxDAO;
    @Autowired
    EmailService emailService;
    @Autowired
    UserRepository userRepository;

    /**
     * Test sans que la date de début ou de fin soit sur un bord du créneau
     */
    @Test
    public void testCenterTimeAndDate(){
        Rendez_vousController rendez_vousController = new Rendez_vousController(creneauxDAO,rendez_vousDAO, emailService, userRepository);
        LocalDateTime heure = LocalDateTime.of(2022,10,11,9,1);
        Duration duree= Duration.ofMinutes(30);
        CreneauxDTO test= rendez_vousController.isWithinASlot(heure,duree, LocalDate.of(2022,8,1));
        Assertions.assertThat(test.getId()).isEqualTo(-2L);
    }

    /**
     * Test quand l'heure de début est l'heure du début du créneau
     */
    @Test
    public void testEdgeTime(){
        Rendez_vousController rendez_vousController = new Rendez_vousController(creneauxDAO,rendez_vousDAO, emailService, userRepository);
        LocalDateTime heure = LocalDateTime.of(2022,10,11,9,0);
        Duration duree= Duration.ofMinutes(30);
        CreneauxDTO test= rendez_vousController.isWithinASlot(heure,duree,LocalDate.of(2022,8,1));
        Assertions.assertThat(test.getId()).isEqualTo(-2L);
    }

    /**
     * Test quand la date de début est la date du début du créneau
     */
    @Test
    public void testEdgeDate(){
        Rendez_vousController rendez_vousController = new Rendez_vousController(creneauxDAO,rendez_vousDAO, emailService, userRepository);
        LocalDateTime heure = LocalDateTime.of(2022,10,10,9,0);
        Duration duree= Duration.ofMinutes(30);
        CreneauxDTO test= rendez_vousController.isWithinASlot(heure,duree,LocalDate.of(2022,8,1));
        Assertions.assertThat(test.getId()).isEqualTo(-2L);
    }

}
