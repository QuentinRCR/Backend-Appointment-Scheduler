package com.docto.protechdoctolib.rendez_vous;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sert à sérialiser et désérialiser l'objet Rendez_vous
 */
public class Rendez_vousDTO {

    private Long id;

    private Long idUser;

    private Long idCreneau;

    private String zoomLink;

    private LocalDateTime dateDebut;

    private String moyenCommunication;

    private Duration duree;


    //Ce constructeur est nécessaire à spring
    public Rendez_vousDTO() {
    }

    public Rendez_vousDTO(Rendez_vous rendez_vous) {
        this.id = rendez_vous.getId();
        this.dateDebut = rendez_vous.getDateDebut();
        this.idUser = rendez_vous.getIdUser();
        this.idCreneau = rendez_vous.getIdCreneau();
        this.duree = rendez_vous.getDuree();
        this.moyenCommunication = rendez_vous.getMoyenCommunication();
        this.zoomLink = rendez_vous.getZoomLink();
    }

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

    public Long getIdCreneau() {
        return idCreneau;
    }

    public void setIdCreneau(Long idCreneau) {
        this.idCreneau = idCreneau;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getMoyenCommunication() {
        return moyenCommunication;
    }

    public void setMoyenCommunication(String moyenCommunication) {
        this.moyenCommunication = moyenCommunication;
    }

    public Duration getDuree() {
        return duree;
    }

    public void setDuree(Duration duree) {
        this.duree = duree;
    }
}