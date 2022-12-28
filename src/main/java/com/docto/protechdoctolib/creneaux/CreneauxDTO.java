package com.docto.protechdoctolib.creneaux;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sert à sérialiser et désérialiser l'objet Créneaux
 */
public class CreneauxDTO {

    private Long id;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private List<DayOfWeek> jours;

    private List<HeuresDebutFinDTO> heuresDebutFin;

    //Ce constructeur est necessaire à spring
    public CreneauxDTO() {
    }

    public CreneauxDTO(Creneaux creneaux) {
        this.id = creneaux.getId();
        this.dateDebut = creneaux.getDateDebut();
        this.dateFin = creneaux.getDateFin();
        this.jours = creneaux.getJours();
        if (creneaux.getHeuresDebutFin()!=null) {
            this.heuresDebutFin = creneaux.getHeuresDebutFin().stream().map(HeuresDebutFinDTO::new).collect(Collectors.toList());
        }
        else{
            this.heuresDebutFin= new ArrayList<HeuresDebutFinDTO>();
        }
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

    public List<HeuresDebutFinDTO> getHeuresDebutFin() {
        return heuresDebutFin;
    }

    public void setHeuresDebutFin(List<HeuresDebutFinDTO> heuresDebutFin) {
        this.heuresDebutFin = heuresDebutFin;
    }
}
