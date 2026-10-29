package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Requête de l'opération {@code updateClient}.
 *
 * <p>Sémantique de mise à jour partielle : un champ {@code null} (élément absent
 * de la requête) reste inchangé ; un champ fourni est validé puis appliqué.
 * L'identifiant est obligatoire et désigne le client à modifier.</p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateClientRequest", namespace = "http://ws.soapclients.ibam.com/")
@XmlRootElement(name = "updateClientRequest", namespace = "http://ws.soapclients.ibam.com/")
public class UpdateClientRequest {

    @XmlElement(required = true)
    private Integer id;

    /** 1 à 100 caractères, non vide — ou {@code null} pour laisser inchangé. */
    private String nom;

    /** 1 à 100 caractères, non vide — ou {@code null} pour laisser inchangé. */
    private String prenom;

    /** Doit respecter le format e-mail et être unique — ou {@code null} pour laisser inchangé. */
    private String email;

    /** 20 caractères maximum — ou {@code null} pour laisser inchangé. */
    private String telephone;

    /** 80 caractères maximum — ou {@code null} pour laisser inchangé. */
    private String ville;

    /** 80 caractères maximum — ou {@code null} pour laisser inchangé. */
    private String pays;

    /** {@code ACTIF} ou {@code INACTIF} — ou {@code null} pour laisser inchangé. */
    private String statut;

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
}
