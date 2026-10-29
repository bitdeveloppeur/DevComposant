package com.ibam.soapclients.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un client de la table {@code clients}.
 *
 * <p>Le schéma de la table est géré par les scripts SQL versionnés
 * ( {@code db/01_schema.sql} ) ; Hibernate est configuré en {@code ddl-auto=validate}
 * et ne crée ni ne modifie jamais le schéma.</p>
 */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "ville", length = 80)
    private String ville;

    @Column(name = "pays", length = 80)
    private String pays;

    @Column(name = "statut", nullable = false, length = 10)
    private String statut = "ACTIF";

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @PrePersist
    void aLaCreation() {
        this.dateCreation = LocalDateTime.now().withNano(0);
        this.dateModification = this.dateCreation;
    }

    @PreUpdate
    void aLaModification() {
        this.dateModification = LocalDateTime.now().withNano(0);
    }

    public Integer getId() {
        return id;
    }

    /** Renseigné par JPA à l'enregistrement ; exposé pour les tests et le mapping. */
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

    public LocalDateTime getDateModification() {
        return dateModification;
    }
}
