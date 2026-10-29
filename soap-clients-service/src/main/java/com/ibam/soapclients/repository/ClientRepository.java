package com.ibam.soapclients.repository;

import com.ibam.soapclients.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Accès aux données clients (Spring Data JPA).
 *
 * <p>Hérite de {@link JpaSpecificationExecutor} pour construire dynamiquement
 * les critères de {@code searchClients} (filtres optionnels) avec l'API Criteria,
 * ce qui garantit des requêtes toujours paramétrées et paginées en base.</p>
 */
public interface ClientRepository extends JpaRepository<Client, Integer>, JpaSpecificationExecutor<Client> {

    /**
     * Recherche un client par e-mail (insensible à la casse), en excluant un identifiant.
     * Sert au contrôle d'unicité de l'e-mail lors d'une mise à jour.
     */
    Optional<Client> findByEmailIgnoreCaseAndIdNot(String email, Integer id);
}
