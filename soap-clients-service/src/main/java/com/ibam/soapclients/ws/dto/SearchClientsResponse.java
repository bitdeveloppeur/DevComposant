package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * Réponse de l'opération {@code searchClients} (page de résultats).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchClientsResponse", namespace = "http://ws.soapclients.ibam.com/")
@XmlRootElement(name = "searchClientsResponse", namespace = "http://ws.soapclients.ibam.com/")
public class SearchClientsResponse {

    @XmlElement(name = "client")
    private List<ClientDetails> clients = new ArrayList<>();

    /** Nombre total de clients correspondant aux critères (toutes pages confondues). */
    @XmlElement(required = true)
    private long total;

    @XmlElement(required = true)
    private int page;

    @XmlElement(required = true)
    private int taillePage;

    public List<ClientDetails> getClients() {
        return clients;
    }

    public void setClients(List<ClientDetails> clients) {
        this.clients = clients;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTaillePage() {
        return taillePage;
    }

    public void setTaillePage(int taillePage) {
        this.taillePage = taillePage;
    }
}
