package com.docto.protechdoctolib.creneaux;


import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Creneaux {

    public Creneaux() { //zzz
    }

    public Creneaux(Long id, LocalDate dateDebut, LocalDate dateFin, List<DayOfWeek> jours, List<HeuresDebutFin> heuresDebutFin) {
        this.id = id;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.jours = jours;
        this.heuresDebutFin = heuresDebutFin;
    }

    /**
     *Id du créneau
     */
     @Id
     @GeneratedValue
     private Long id;

    /**
     * Premier jour de la plage de temps
     */
    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
     private LocalDate dateDebut;

    /**
     * Dernier jour de la plage de temps
     */
    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
     private LocalDate dateFin;

    /**
     * Liste des jours pour lesquels on peut prendre des rendez-vous
     */
    @ElementCollection(targetClass = DayOfWeek.class)
    @Enumerated(EnumType.STRING)
    @Column
    private List<DayOfWeek> jours;

    /**
     * Plages de temps pendant lesquels on peut prendre un rendez-vous. Ex: entre 8h et 12h et entre 14h et 18h
     */
    @OneToMany(mappedBy = "idCreneaux")
    private List<HeuresDebutFin> heuresDebutFin;


    public List<HeuresDebutFin> getHeuresDebutFin() {
        return heuresDebutFin;
    }

    public void setHeuresDebutFin(List<HeuresDebutFin> heuresDebutFin) {
        this.heuresDebutFin = heuresDebutFin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public List<DayOfWeek> getJours() {
        return jours;
    }

    public void setJours(List<DayOfWeek> jours) {
        this.jours = jours;
    }

}
