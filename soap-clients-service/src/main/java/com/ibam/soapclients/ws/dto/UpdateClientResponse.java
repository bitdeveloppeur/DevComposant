package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Réponse de l'opération {@code updateClient} : état complet du client après
 * modification (horodatage {@code dateModification} actualisé).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateClientResponse", namespace = "http://ws.soapclients.ibam.com/")
@XmlRootElement(name = "updateClientResponse", namespace = "http://ws.soapclients.ibam.com/")
public class UpdateClientResponse {

    @XmlElement(required = true)
    private ClientDetails client;

    public ClientDetails getClient() {
        return client;
    }

    public void setClient(ClientDetails client) {
        this.client = client;
    }
}
