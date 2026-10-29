package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * Détail porté par une erreur SOAP (élément {@code <detail>} de la Fault).
 *
 * <ul>
 *   <li>{@code code} : code fonctionnel stable (ex. {@code CLIENT_INTROUVABLE}),
 *       exploitable par les clients pour brancher leur gestion d'erreurs ;</li>
 *   <li>{@code message} : message compréhensible, sans détail interne (sécurité) ;</li>
 *   <li>{@code erreurs} : le cas échéant, détail champ par champ (règles de validation).</li>
 * </ul>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceFault", namespace = "http://ws.soapclients.ibam.com/", propOrder = {"code", "message", "erreurs"})
public class ServiceFault {

    @XmlElement(required = true)
    private String code;

    @XmlElement(required = true)
    private String message;

    @XmlElement(name = "erreur")
    private List<String> erreurs = new ArrayList<>();

    public ServiceFault() {
    }

    public ServiceFault(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceFault(String code, String message, List<String> erreurs) {
        this(code, message);
        this.erreurs = erreurs;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErreurs() {
        return erreurs;
    }

    public void setErreurs(List<String> erreurs) {
        this.erreurs = erreurs;
    }
}
