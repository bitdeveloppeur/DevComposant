package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Requête de l'opération {@code searchClients}.
 *
 * <p>Tous les filtres sont optionnels et combinés par « ET ». Sans filtre,
 * la recherche retourne l'ensemble des clients (paginés).</p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchClientsRequest", namespace = "http://ws.soapclients.ibam.com/")
@XmlRootElement(name = "searchClientsRequest", namespace = "http://ws.soapclients.ibam.com/")
public class SearchClientsRequest {

    /** Fragment de nom (insensible à la casse) ou {@code null} pour ignorer le filtre. */
    private String nom;

    /** Fragment de ville (insensible à la casse) ou {@code null} pour ignorer le filtre. */
    private String ville;

    /** Statut exact : {@code ACTIF} ou {@code INACTIF}, ou {@code null} pour ignorer le filtre. */
    private String statut;

    /** Numéro de page demandé, commence à 0 (défaut 0). */
    private Integer page = 0;

    /** Taille de page souhaitée, bornée à 100 (défaut 20). */
    private Integer taillePage = 20;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTaillePage() {
        return taillePage;
    }

    public void setTaillePage(Integer taillePage) {
        this.taillePage = taillePage;
    }
}
