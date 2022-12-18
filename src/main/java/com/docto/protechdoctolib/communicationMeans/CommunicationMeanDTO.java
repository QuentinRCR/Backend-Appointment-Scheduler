package com.docto.protechdoctolib.communicationMeans;

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
