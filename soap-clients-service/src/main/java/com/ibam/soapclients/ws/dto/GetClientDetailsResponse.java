package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Réponse de l'opération {@code getClientDetails}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetClientDetailsResponse", namespace = "http://ws.soapclients.ibam.com/")
@XmlRootElement(name = "getClientDetailsResponse", namespace = "http://ws.soapclients.ibam.com/")
public class GetClientDetailsResponse {

    @XmlElement(required = true)
    private ClientDetails client;

    public ClientDetails getClient() {
        return client;
    }

    public void setClient(ClientDetails client) {
        this.client = client;
    }
}
