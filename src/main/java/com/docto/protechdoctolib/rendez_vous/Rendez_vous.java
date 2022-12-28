package com.docto.protechdoctolib.rendez_vous;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Modèle définissant les attributs d'un Créneau
 */
@Entity
public class Rendez_vous {

    public Rendez_vous() {
    }

    public Rendez_vous(Long id, Long idCreneau, Long idUser, LocalDateTime dateDebut, Duration duree, String moyenCommunication, String zoomLink) {
        this.id = id;
        this.dateDebut = dateDebut;
        this.idCreneau= idCreneau;
        this.idUser=idUser;
        this.duree=duree;
        this.moyenCommunication= moyenCommunication;
        this.zoomLink=zoomLink;
    }


    /**
     * Id du rdv
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Id de l'utilisateur qui prend le rdv
     */
    @Column
    private Long idUser;

    /**
     * Id du creneau qui contient le rdv
     */
    @Column
    private long idCreneau;

    /**
     * Durée du rdv
     */
    @Column
    private Duration duree;

    /**
     * Moyen de communication choisi
     */
    @Column
    private String moyenCommunication;

    /**
     * Date et heure du début du rendez-vous
     */
    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime dateDebut;

    /**
     * lien zoom du rdv
     */
    @Column
    private String zoomLink;

    /**
     * Getters et Setters
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public long getIdCreneau() {
        return idCreneau;
    }

    public void setIdCreneau(long idCreneau) {
        this.idCreneau = idCreneau;
    }

    public Duration getDuree() {
        return duree;
    }

    public void setDuree(Duration duree) {
        this.duree = duree;
    }

    public String getMoyenCommunication() {
        return moyenCommunication;
    }

    public void setMoyenCommunication(String moyenCommunication) {
        this.moyenCommunication = moyenCommunication;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }
}

