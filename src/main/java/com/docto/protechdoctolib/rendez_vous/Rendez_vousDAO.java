package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.creneaux.Creneaux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface Rendez_vousDAO extends JpaRepository<Rendez_vous, Long> {

    /**
     * Permet de trouver tous les rendez-vous pris par un client
     * @param idClient
     * @return Liste des rendez-vous pris par le client
     */
    @Query("select c from Rendez_vous c where c.idUser=:idClient")  // (2)
    List<Rendez_vous> findAllByIdUser(@Param("idClient") Long idClient);

    /**
     * Permet de trouver tous les rendez-vous qui sont dans un créneau
     * @param idCreneau
     * @return liste de tous les rendez-vous qui sont dans un créneau
     */
    @Query("select c from Rendez_vous c where c.idCreneau=:idCreneau")  // (2)
    List<Rendez_vous> findAllByIdCreneau(@Param("idCreneau") Long idCreneau);

}
