package com.docto.protechdoctolib.rendez_vous;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class Rendez_vousDAOtest {

    @Autowired
    private Rendez_vousDAO rendez_vousDAO;

    /**
     * Teste si le rdv -1 contient bien les bonnes informations
     */
    @Test
    public void shouldFindARdv() {
        Rendez_vous rendez_vous = rendez_vousDAO.getReferenceById(-1L); //rendez_vousDAO.getReferenceById(-2L);
        LocalDateTime dateDebut = LocalDateTime.of(2022, 10, 16, 0, 0, 0);
        Long idUser = Long.valueOf(2);
        Long idCreneau = Long.valueOf(3);
        Duration duree = Duration.ofSeconds(4);
        String moyenComm = String.valueOf("zoom");
        String zoomLink = String.valueOf("link.fr");
        Rendez_vous rdvTest = new Rendez_vous(-1L, idCreneau, idUser, dateDebut, duree, moyenComm, zoomLink);
        Assertions.assertThat(rendez_vous.getDateDebut()).isEqualTo(rdvTest.getDateDebut());
        Assertions.assertThat(rendez_vous.getIdCreneau()).isEqualTo(rdvTest.getIdCreneau());
        Assertions.assertThat(rendez_vous.getIdUser()).isEqualTo(rdvTest.getIdUser());
        Assertions.assertThat(rendez_vous.getMoyenCommunication()).isEqualTo(rdvTest.getMoyenCommunication());
        Assertions.assertThat(rendez_vous.getZoomLink()).isEqualTo(rdvTest.getZoomLink());
        Assertions.assertThat(rendez_vous.getDuree()).isEqualTo(rdvTest.getDuree());


    }

    /**
     * Teste si la liste de rdv retournée a bien une taille de 2 comme dans le import.sql
     */
    @Test
    public void shouldFind2Creneaux() {
        List<Rendez_vous> rdvs = rendez_vousDAO.findAll();
        Assertions.assertThat(rdvs.size()).isEqualTo(2);
    }

    /**
     * Teste si ça supprime bien le créneaux ayant pour id 1
     */
    @Test
    public void shouldDeleteCreneaux(){
        rendez_vousDAO.deleteById(-1L);
        List<Rendez_vous> rdvs = rendez_vousDAO.findAll();
        Assertions.assertThat(rdvs.size()).isEqualTo(1);
        Assertions.assertThat(rdvs.get(0).getId()).isEqualTo(-2);
    }

    /**
     * Teste si ça modifie bien le temps de fin du créneau ayant l'id 1
     */
    @Test
    public void shoudModifyCreneau1(){
        Rendez_vous rendez_vous = rendez_vousDAO.getReferenceById(-1L);
        rendez_vous.setDateDebut(LocalDateTime.of(2695,12,30,0,0,1));
        Rendez_vous rendez_vous1 = rendez_vousDAO.getReferenceById(-1L);
        Assertions.assertThat(rendez_vous1.getDateDebut()).isEqualTo(LocalDateTime.of(2695,12,30,0,0,1));
    }




}