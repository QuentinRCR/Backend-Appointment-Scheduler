package com.docto.protechdoctolib.communicationMeans;

/**
 * Sert à sérialiser et désérialiser l'objet CommunicationMeans
 */
public class CommunicationMeanDTO {

    private String communicationMean;

    public CommunicationMeanDTO() {
    }

    public CommunicationMeanDTO(CommunicationMeans communicationMeans) {
        this.communicationMean=communicationMeans.getCommunicationMean();
    }

    public String getCommunicationMean() {
        return communicationMean;
    }

    public void setCommunicationMean(String communicationMean) {
        this.communicationMean = communicationMean;
    }
}
