package com.docto.protechdoctolib.creneaux;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalTime;

/**
 * Modèle définissant une plage de temps dans une journée (ex: entre 8h et 12h). Ce modèle est utilisé par l'objet Créneau dans le but de pouvoir avoir plusieurs plages horaires dans un créneau
 */
@Entity
public class HeuresDebutFin {

    public HeuresDebutFin() {
    }

    public HeuresDebutFin(Long id, Long idCreneaux, LocalTime tempsDebut, LocalTime tempsFin) {
        this.idPlage = id;
        this.idCreneaux = idCreneaux;
        this.tempsDebut = tempsDebut;
        this.tempsFin = tempsFin;
    }

    public  HeuresDebutFin(HeuresDebutFinDTO heuresDebutFinDTO){
        this.idPlage=heuresDebutFinDTO.getIdPlage();
        this.tempsDebut=heuresDebutFinDTO.getTempsDebut();
        this.tempsFin=heuresDebutFinDTO.getTempsFin();
        this.idCreneaux= heuresDebutFinDTO.getIdCreneaux();
    }

    @Id
    @GeneratedValue
    private Long idPlage;

    /**
     * Id du créneau auquel la plage correspond
     */
    private  Long idCreneaux;

    /**
     * Heure de debut de la plage de temps
     */
    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
    private LocalTime tempsDebut;

    /**
     * Heure de fin de la plage de temps
     */
    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
    private LocalTime tempsFin;

    public Long getIdPlage() {
        return idPlage;
    }

    public void setIdPlage(Long idPlage) {
        this.idPlage = idPlage;
    }

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
}
