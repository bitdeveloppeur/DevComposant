package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;

/**
 * Représentation d'un client dans les réponses SOAP.
 *
 * <p>L'ordre des champs est fixé par {@code propOrder} afin que le XML produit
 * soit stable et documentable dans le WSDL.</p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientDetails", namespace = "http://ws.soapclients.ibam.com/", propOrder = {
        "id", "nom", "prenom", "email", "telephone",
        "ville", "pays", "statut", "dateCreation", "dateModification"})
public class ClientDetails {

    @XmlElement(required = true)
    private Integer id;

    @XmlElement(required = true)
    private String nom;

    @XmlElement(required = true)
    private String prenom;

    @XmlElement(required = true)
    private String email;

    private String telephone;

    private String ville;

    private String pays;

    @XmlElement(required = true)
    private String statut;

    /** Date au format ISO-8601 ( {@code yyyy-MM-dd'T'HH:mm:ss} ). */
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    private LocalDateTime dateCreation;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    private LocalDateTime dateModification;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }
}
