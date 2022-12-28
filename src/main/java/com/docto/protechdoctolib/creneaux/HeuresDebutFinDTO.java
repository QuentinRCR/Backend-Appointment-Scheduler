package com.docto.protechdoctolib.creneaux;

import java.time.LocalTime;

/**
 * Sert à sérialiser et désérialiser l'objet HeureDebutFin
 */
public class HeuresDebutFinDTO {

    public HeuresDebutFinDTO() {
    }

    public HeuresDebutFinDTO(HeuresDebutFin heuresDebutFin) {
        this.idPlage=heuresDebutFin.getIdPlage();
        this.idCreneaux = heuresDebutFin.getIdCreneaux();
        this.tempsDebut = heuresDebutFin.getTempsDebut();
        this.tempsFin = heuresDebutFin.getTempsFin();
    }

    private  Long idPlage;

    private  Long idCreneaux;

    private LocalTime tempsDebut;

    private LocalTime tempsFin;

    public Long getIdCreneaux() {
        return idCreneaux;
    }

    public void setIdCreneaux(Long idCreneaux) {
        this.idCreneaux = idCreneaux;
    }

    public LocalTime getTempsDebut() {
        return tempsDebut;
    }

    public void setTempsDebut(LocalTime tempsDebut) {
        this.tempsDebut = tempsDebut;
    }

    public LocalTime getTempsFin() {
        return tempsFin;
    }

    public void setTempsFin(LocalTime tempsFin) {
        this.tempsFin = tempsFin;
    }

    public Long getIdPlage() {
        return idPlage;
    }

    public void setIdPlage(Long idPlage) {
        this.idPlage = idPlage;
    }
}
