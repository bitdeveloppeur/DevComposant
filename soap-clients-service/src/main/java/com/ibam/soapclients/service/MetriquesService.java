package com.ibam.soapclients.service;

import com.ibam.soapclients.repository.ClientRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Métriques métier du service (Micrometer, exposées sur /actuator/prometheus) :
 *
 * <ul>
 *   <li>{@code soap_requests_total} — compteur d'appels par opération et par
 *       résultat ({@code succes} / {@code fault}) : base des panneaux « appels/s »
 *       et « taux de Faults » du tableau de bord Grafana ;</li>
 *   <li>{@code clients_in_db} — jauge : nombre de clients en base, rafraîchie
 *       périodiquement (elle matérialise l'effet du chargement NiFi).</li>
 * </ul>
 */
@Service
public class MetriquesService {

    static final String METRIQUE_APPELS = "soap_requests";
    static final String METRIQUE_CLIENTS = "clients_in_db";

    private final MeterRegistry registry;
    private final ClientRepository repository;

    /** Référence détenue fortement : évite la collecte du gauge par référence faible. */
    private final AtomicLong totalClients = new AtomicLong();

    public MetriquesService(MeterRegistry registry, ClientRepository repository) {
        this.registry = registry;
        this.repository = repository;
        registry.gauge(METRIQUE_CLIENTS, totalClients, Number::doubleValue);
    }

    /**
     * Incrémente le compteur d'appels pour une opération et un résultat donnés.
     *
     * @param operation nom de l'opération SOAP (ex. {@code getClientDetails})
     * @param succes    {@code true} si l'appel a abouti, {@code false} s'il a
     *                  produit une Fault SOAP
     */
    public void enregistrerAppel(String operation, boolean succes) {
        registry.counter(METRIQUE_APPELS,
                        "operation", operation,
                        "resultat", succes ? "succes" : "fault")
                .increment();
    }

    /** Rafraîchit la jauge du nombre de clients (requête COUNT indexée, table petite). */
    @Scheduled(fixedDelay = 15_000)
    void rafraichirTotalClients() {
        totalClients.set(repository.count());
    }
}
