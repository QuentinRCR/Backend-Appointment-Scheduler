package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.creneaux.Creneaux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Permet de lier la base de donnée à la representation dans java de l'objet Rendez-vous
 */
public interface Rendez_vousDAO extends JpaRepository<Rendez_vous, Long> {

    /**
     * Permet de trouver tous les rendez-vous pris par un client
     * @param idClient
     * @return Liste des rendez-vous pris par le client
     */
    @Query("select c from Rendez_vous c where c.idUser=:idClient order by c.dateDebut desc ")  // (2)
    List<Rendez_vous> findAllByIdUser(@Param("idClient") Long idClient);

    /**
     * Renvoie tous les rendez-vous par ordre croissant de date de début
     * @return tous les rendez-vous par ordre croissant de date de début
     */
    @Query("select c from Rendez_vous c order by c.dateDebut desc")  // (2)
    List<Rendez_vous> findAll();

    /**
     * Renvoie tous les rendez-vous qui appartiennent à un créneau
     * @param idCreneau
     * @return liste de tous les rendez-vous qui appartiennent à un créneau
     */
    @Query("select c from Rendez_vous c where c.idCreneau=:idCreneau")  // (2)
    List<Rendez_vous> findAllByIdCreneau(@Param("idCreneau") Long idCreneau);

    /**
     * Renvoie tous les rendez-vous qui ont pour date de début la date fournie
     * @param dateDebut
     * @return tous les rendez-vous qui ont pour date de début la date fournie
     */
    @Query("select c from Rendez_vous c where c.dateDebut=:dateDebut")  // (2)
    List<Rendez_vous> findByDateDebut(@Param("dateDebut") LocalDateTime dateDebut);

    /**
     * Renvoie tous les rendez-vous qui sont après la date fournie
     * @param datee
     * @return tous les rendez-vous qui sont après la date fournie
     */
    @Query("select c from Rendez_vous c where c.dateDebut>=:datee")  // (2)
    List<Rendez_vous> findRendez_vousAfterDate(@Param("datee") LocalDateTime datee);

    /**
     * Renvoie tous les rendez-vous qui sont entre la date de début et la date de fin fourni
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("select c from Rendez_vous c where c.dateDebut>=:startDate and c.dateDebut<=:endDate")
    List<Rendez_vous> export(@Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate);

}
