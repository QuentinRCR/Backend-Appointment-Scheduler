package com.docto.protechdoctolib.communicationMeans;


import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Permet de lier la base de donnée à la representation dans java de l'objet CommunicationMean
 */
public interface CommunicationMeanDAO  extends JpaRepository<CommunicationMeans, Long> {
}
