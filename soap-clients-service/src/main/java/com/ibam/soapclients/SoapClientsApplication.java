package com.ibam.soapclients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée du service SOAP de gestion des clients (Exercice 2).
 *
 * <p>Stack technique : Spring Boot 3, Apache CXF (JAX-WS), Spring Data JPA / MySQL,
 * Actuator + Micrometer (métriques Prometheus), observées par Grafana.</p>
 *
 * <p>Endpoints principaux une fois l'application démarrée :</p>
 * <ul>
 *   <li>WSDL du service : {@code http://localhost:8080/services/clients?wsdl}</li>
 *   <li>Santé : {@code http://localhost:8080/actuator/health}</li>
 *   <li>Métriques : {@code http://localhost:8080/actuator/prometheus}</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class SoapClientsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoapClientsApplication.class, args);
    }
}
