package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Requête de l'opération {@code getClientDetails}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetClientDetailsRequest", namespace = "http://ws.soapclients.ibam.com/")
@XmlRootElement(name = "getClientDetailsRequest", namespace = "http://ws.soapclients.ibam.com/")
public class GetClientDetailsRequest {

    /** Identifiant technique du client recherché (strictement positif). */
    @XmlElement(required = true)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
