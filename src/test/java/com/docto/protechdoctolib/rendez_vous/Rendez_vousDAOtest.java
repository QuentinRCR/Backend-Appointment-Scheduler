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
//    @Test
//    public void shouldFindARdv() {
//        Rendez_vous rendez_vous = rendez_vousDAO.getReferenceById(-1L); //rendez_vousDAO.getReferenceById(-2L);
//        LocalDateTime dateDebut = LocalDateTime.of(2022, 10, 16, 0, 0, 0);
//        Long idUser = Long.valueOf(-11);
//        Long idCreneau = Long.valueOf(-1);
//        Duration duree = Duration.ofSeconds(4);
//        String moyenComm = String.valueOf("zoom");
//        String zoomLink = String.valueOf("link.fr");
//        Rendez_vous rdvTest = new Rendez_vous(-1L, idCreneau, idUser, dateDebut, duree, moyenComm, zoomLink);
//        Assertions.assertThat(rendez_vous.getDateDebut()).isEqualTo(rdvTest.getDateDebut());
//        Assertions.assertThat(rendez_vous.getIdCreneau()).isEqualTo(rdvTest.getIdCreneau());
//        Assertions.assertThat(rendez_vous.getIdUser()).isEqualTo(rdvTest.getIdUser());
//        Assertions.assertThat(rendez_vous.getMoyenCommunication()).isEqualTo(rdvTest.getMoyenCommunication());
//        Assertions.assertThat(rendez_vous.getZoomLink()).isEqualTo(rdvTest.getZoomLink());
//        Assertions.assertThat(rendez_vous.getDuree()).isEqualTo(rdvTest.getDuree());
//
//
//    }

    /**
     * Teste si la liste de rdv retournée a bien une taille de 3 comme dans le import.sql
     */
    @Test
    public void shouldFind3RDV() {
        List<Rendez_vous> rdvs = rendez_vousDAO.findAll();
        Assertions.assertThat(rdvs.size()).isEqualTo(3);
    }

    /**
     * Teste si ça supprime bien le RDV ayant pour id -1
     */
    @Test
    public void shouldDeleteRDV(){
        rendez_vousDAO.deleteById(-1L);
        List<Rendez_vous> rdvs = rendez_vousDAO.findAll();
        Assertions.assertThat(rdvs.size()).isEqualTo(2);
        Assertions.assertThat(rdvs.get(0).getId()).isEqualTo(-3);
    }

    /**
     * Teste si ça modifie bien le temps de fin du RDV ayant l'id -1
     */
    @Test
    public void shoudModifyRDV1(){
        Rendez_vous rendez_vous = rendez_vousDAO.getReferenceById(-1L);
        rendez_vous.setDateDebut(LocalDateTime.of(2695,12,30,0,0,1));
        Rendez_vous rendez_vous1 = rendez_vousDAO.getReferenceById(-1L);
        Assertions.assertThat(rendez_vous1.getDateDebut()).isEqualTo(LocalDateTime.of(2695,12,30,0,0,1));
    }

    /**
     * Test qu'on obtient bien les créneaux avec le bon idClient
     */
    @Test
    public void shouldGetRDVForClientId3(){
        List<Rendez_vous> listRendezVous = rendez_vousDAO.findAllByIdUser(-10L);
        Assertions.assertThat(listRendezVous.size()).isEqualTo(2);
        Assertions.assertThat(listRendezVous.get(0).getIdUser()).isEqualTo(-10L);
    }

    @Test
    public void shouldGetAppointementsWithIDCreneau4(){
        List<Rendez_vous> listRendezVous = rendez_vousDAO.findAllByIdCreneau(-2L);
        Assertions.assertThat(listRendezVous.size()).isEqualTo(2);
        Assertions.assertThat(listRendezVous.get(0).getIdCreneau()).isEqualTo(-2L);
    }


}