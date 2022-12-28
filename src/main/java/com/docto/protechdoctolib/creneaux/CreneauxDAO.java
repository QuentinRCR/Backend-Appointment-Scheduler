package com.docto.protechdoctolib.creneaux;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Permet de lier la base de donnée à la representation dans java de l'objet Créneau
 */
public interface CreneauxDAO extends JpaRepository<Creneaux, Long> {

    /**
     * Revoie tous les créneaux qui finissent après une date donnée
     * @param datee
     * @return Liste de créneaux commençant après la date
     */
    @Query("select c from Creneaux c where c.dateFin>=:datee")  // (2)
    List<Creneaux> findCreneauxAfterDate(@Param("datee") LocalDate datee);

    /**
     * Renvoie une liste de Créneaux triée par ordre décroissant de date de fin afin qu'ils s'affichent dans le bon ordre dans le frontend
     * @return
     */
    @Query("select c from Creneaux c order by c.dateFin desc")  // (2)
    List<Creneaux> findAll();

}
