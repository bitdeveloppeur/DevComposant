package com.ibam.soapclients.service;

import com.ibam.soapclients.exception.ClientIntrouvableException;
import com.ibam.soapclients.exception.DonneesInvalidesException;
import com.ibam.soapclients.model.Client;
import com.ibam.soapclients.repository.ClientRepository;
import com.ibam.soapclients.ws.dto.UpdateClientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de la logique métier (repository simulé — aucune base requise).
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    private ClientService service;

    @BeforeEach
    void preparer() {
        service = new ClientService(repository);
    }

    private static Client client(int id) {
        Client client = new Client();
        client.setId(id);
        client.setNom("Ndiaye");
        client.setPrenom("Amadou");
        client.setEmail("amadou.ndiaye@example.com");
        client.setStatut("ACTIF");
        return client;
    }

    // ------------------------------------------------------------- consulter

    @Test
    @DisplayName("consulter : identifiant null ou non positif → DONNEES_INVALIDES")
    void consulterIdentifiantInvalide() {
        assertThatThrownBy(() -> service.consulter(null))
                .isInstanceOf(DonneesInvalidesException.class);
        assertThatThrownBy(() -> service.consulter(0))
                .isInstanceOf(DonneesInvalidesException.class);
    }

    @Test
    @DisplayName("consulter : client absent → CLIENT_INTROUVABLE")
    void consulterClientIntrouvable() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consulter(999))
                .isInstanceOf(ClientIntrouvableException.class);
    }

    @Test
    @DisplayName("consulter : retourne le client correspondant")
    void consulterOk() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(client(1)));

        Client resultat = service.consulter(1);

        assertThat(resultat.getId()).isEqualTo(1);
        assertThat(resultat.getEmail()).isEqualTo("amadou.ndiaye@example.com");
    }

    // ------------------------------------------------------------ rechercher

    @Test
    @DisplayName("rechercher : statut hors énumération → DONNEES_INVALIDES")
    void rechercherStatutInvalide() {
        assertThatThrownBy(() -> service.rechercher(null, null, "SUSPENDU", 0, 20))
                .isInstanceOf(DonneesInvalidesException.class);
    }

    @Test
    @DisplayName("rechercher : taille de page bornée à 100, défaut 20, tri par id")
    void rechercherBornesPagination() throws Exception {
        Page<Client> pageVide = new PageImpl<>(List.of());
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageVide);

        service.rechercher("diallo", "Dakar", "actif", -5, 5000);

        ArgumentCaptor<Pageable> paginateur = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), paginateur.capture());
        assertThat(paginateur.getValue().getPageNumber()).isZero();
        assertThat(paginateur.getValue().getPageSize()).isEqualTo(ClientService.TAILLE_PAGE_MAX);
        assertThat(paginateur.getValue().getSort().getOrderFor("id")).isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("rechercher : sans résultat retourne une page vide (pas une erreur)")
    void rechercherSansResultat() throws Exception {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<Client> resultat = service.rechercher("zzzz", null, null, null, null);

        assertThat(resultat.getTotalElements()).isZero();
    }

    // ---------------------------------------------------------- mettreAJour

    @Test
    @DisplayName("mettreAJour : identifiant manquant → DONNEES_INVALIDES")
    void mettreAJourIdentifiantManquant() {
        assertThatThrownBy(() -> service.mettreAJour(new UpdateClientRequest()))
                .isInstanceOf(DonneesInvalidesException.class);
    }

    @Test
    @DisplayName("mettreAJour : client absent → CLIENT_INTROUVABLE")
    void mettreAJourClientIntrouvable() {
        UpdateClientRequest requete = new UpdateClientRequest();
        requete.setId(999);
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.mettreAJour(requete))
                .isInstanceOf(ClientIntrouvableException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pas-un-email", "a@@b.com", "a b@c.com"})
    @DisplayName("mettreAJour : e-mail mal formé → DONNEES_INVALIDES avec détail")
    void mettreAJourEmailInvalide(String email) {
        UpdateClientRequest requete = new UpdateClientRequest();
        requete.setId(1);
        requete.setEmail(email);

        // La validation est levée avant tout accès à la base.
        assertThatThrownBy(() -> service.mettreAJour(requete))
                .isInstanceOf(DonneesInvalidesException.class)
                .hasMessageContaining("erreur(s) de validation");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("mettreAJour : champ fourni vide ou réduit à des espaces → DONNEES_INVALIDES")
    void mettreAJourChampVide(String nom) {
        UpdateClientRequest requete = new UpdateClientRequest();
        requete.setId(1);
        requete.setNom(nom);

        // La validation est levée avant tout accès à la base.
        assertThatThrownBy(() -> service.mettreAJour(requete))
                .isInstanceOf(DonneesInvalidesException.class);
    }

    @Test
    @DisplayName("mettreAJour : e-mail déjà pris par un autre client → DONNEES_INVALIDES")
    void mettreAJourEmailDejaUtilise() {
        UpdateClientRequest requete = new UpdateClientRequest();
        requete.setId(1);
        requete.setEmail("fatou.diallo@example.com");
        when(repository.findById(1)).thenReturn(Optional.of(client(1)));
        when(repository.findByEmailIgnoreCaseAndIdNot("fatou.diallo@example.com", 1))
                .thenReturn(Optional.of(client(2)));

        assertThatThrownBy(() -> service.mettreAJour(requete))
                .isInstanceOf(DonneesInvalidesException.class)
                .hasMessageContaining("déjà utilisée");
    }

    @Test
    @DisplayName("mettreAJour : seuls les champs fournis sont appliqués et normalisés")
    void mettreAJourApplicationPartielle() throws Exception {
        Client existant = client(5);
        UpdateClientRequest requete = new UpdateClientRequest();
        requete.setId(5);
        requete.setVille("  Marrakech ");
        requete.setStatut("inactif");
        when(repository.findById(5)).thenReturn(Optional.of(existant));
        when(repository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client resultat = service.mettreAJour(requete);

        // Champs absents de la requête : inchangés.
        assertThat(resultat.getNom()).isEqualTo("Ndiaye");
        assertThat(resultat.getEmail()).isEqualTo("amadou.ndiaye@example.com");
        // Champs fournis : appliqués après nettoyage / normalisation.
        assertThat(resultat.getVille()).isEqualTo("Marrakech");
        assertThat(resultat.getStatut()).isEqualTo("INACTIF");
        verify(repository).save(eq(existant));
    }
}
