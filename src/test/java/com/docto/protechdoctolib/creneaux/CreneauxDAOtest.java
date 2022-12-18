
package com.docto.protechdoctolib.creneaux;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class CreneauxDAOtest {

    @Autowired
    private CreneauxDAO creneauxDAO;

    @Autowired
    private  HeuresDebutFinDAO heuresDebutFinDAO;

    /**
     * Teste si quand on demande le créneau -1, on obtient bien celui-là
      */
    @Test
    public void shouldFindSlotWithIdMinus1() {
        Creneaux creneaux =creneauxDAO.getReferenceById(-1L); //creneauxDAO.getReferenceById(2000L);
        Assertions.assertThat(creneaux.getId()).isEqualTo(-1);
    }

    /**
     * Test qu'on récupère bien tous les créneaux
     */
    @Test
    public void shouldFind2Creneaux() {
        List<Creneaux> creneaux = creneauxDAO.findAll();
        Assertions.assertThat(creneaux.size()).isEqualTo(2);
    }

    /**
     * Test que les créneaux sont bien triés par date de fin décroissante dans le résultat du findAll
     */
    @Test
    public void souldFindOrderedByEndDate(){
        List<Creneaux> creneaux = creneauxDAO.findAll();
        Assertions.assertThat(creneaux.get(0).getDateFin()).isAfter(creneaux.get(1).getDateFin());
    }

    /**
     * Test si ça supprime bien le créneau ayant pour id -1
     */
    @Test
    public void shouldDeleteCreneaux(){
        creneauxDAO.deleteById(-1L);
        List<Creneaux> creneaux = creneauxDAO.findAll();
        Assertions.assertThat(creneaux.get(0).getId()).isEqualTo(-2); //And that the remaining slot as id -2
    }

    /**
     * Test si ça modifie bien le temps de fin du créneau ayant l'id -1
     */
    @Test
    public void shoudModifyCreneau1(){
        Creneaux creneaux = creneauxDAO.getReferenceById(-1L);
        creneaux.setDateDebut(LocalDate.of(2695,12,30));
        Creneaux creneaux1 = creneauxDAO.getReferenceById(-1L);
        Assertions.assertThat(creneaux1.getDateDebut()).isEqualTo(LocalDate.of(2695,12,30));
    }

    /**
     * Test que la fonction findCreneauxAfterDate trouve bien uniquement les créneaux finissant après une certaine date
     */
    @Test
    public void findCreneauxAfterDate(){
        List<Creneaux> creneaux = creneauxDAO.findCreneauxAfterDate(LocalDate.of(2022,10,11));
        Assertions.assertThat(creneaux.get(0).getDateFin()).isAfter(LocalDate.of(2022,10,11));
    }
}