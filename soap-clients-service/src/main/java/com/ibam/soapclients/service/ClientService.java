package com.ibam.soapclients.service;

import com.ibam.soapclients.exception.ClientIntrouvableException;
import com.ibam.soapclients.exception.DonneesInvalidesException;
import com.ibam.soapclients.model.Client;
import com.ibam.soapclients.repository.ClientRepository;
import com.ibam.soapclients.repository.ClientSpecifications;
import com.ibam.soapclients.ws.dto.UpdateClientRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Logique métier du service de clients : consultation, recherche paginée et
 * mise à jour, avec validation des entrées (règles du contrat {@code docs/contrat-ws.md}).
 *
 * <p>Toutes les requêtes construites passent par Spring Data / l'API Criteria
 * (requêtes paramétrées) : aucune concaténation SQL n'intervient nulle part.</p>
 */
@Service
public class ClientService {

    /** Taille de page appliquée quand la requête n'en précise pas. */
    public static final int TAILLE_PAGE_DEFAUT = 20;

    /** Taille de page maximale admise (protège la base et le réseau). */
    public static final int TAILLE_PAGE_MAX = 100;

    /**
     * Format e-mail admis (suffisant pour le métier, sans la complexité
     * intégrale de la RFC 5322) : {@code local@domaine[.sous-domaine]}.
     */
    static final Pattern FORMAT_EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");

    static final Set<String> STATUTS_ADMIS = Set.of("ACTIF", "INACTIF");

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    /**
     * Retourne le client identifié par {@code id}.
     *
     * @throws DonneesInvalidesException  si l'identifiant est absent ou non positif
     * @throws ClientIntrouvableException si aucun client ne porte cet identifiant
     */
    @Transactional(readOnly = true)
    public Client consulter(Integer id)
            throws DonneesInvalidesException, ClientIntrouvableException {
        if (id == null || id <= 0) {
            throw DonneesInvalidesException.pourMessage(
                    "L'identifiant du client est obligatoire et doit être un entier strictement positif");
        }
        return repository.findById(id)
                .orElseThrow(() -> ClientIntrouvableException.pourId(id));
    }

    /**
     * Recherche paginée : filtres optionnels (nom, ville, statut) combinés par « ET »,
     * insensibles à la casse. Une recherche sans résultat n'est pas une erreur.
     *
     * @throws DonneesInvalidesException si le statut fourni n'est pas admis
     */
    @Transactional(readOnly = true)
    public Page<Client> rechercher(String nom, String ville, String statut,
                                    Integer page, Integer taillePage)
            throws DonneesInvalidesException {
        if (StringUtils.hasText(statut)
                && !STATUTS_ADMIS.contains(statut.trim().toUpperCase(Locale.ROOT))) {
            throw DonneesInvalidesException.pourMessage(
                    "Statut invalide '" + statut.trim() + "' : valeurs admises ACTIF, INACTIF");
        }
        int numeroPage = (page == null || page < 0) ? 0 : page;
        int taille = (taillePage == null || taillePage <= 0)
                ? TAILLE_PAGE_DEFAUT
                : Math.min(taillePage, TAILLE_PAGE_MAX);

        return repository.findAll(
                ClientSpecifications.recherche(nom, ville, statut),
                PageRequest.of(numeroPage, taille, Sort.by(Sort.Direction.ASC, "id")));
    }

    /**
     * Applique la mise à jour partielle décrite par {@code requete} : chaque champ
     * fourni est validé puis appliqué, les champs absents restent inchangés.
     *
     * @throws DonneesInvalidesException  si un champ fourni viole une règle
     * @throws ClientIntrouvableException si l'identifiant ne correspond à aucun client
     */
    @Transactional
    public Client mettreAJour(UpdateClientRequest requete)
            throws DonneesInvalidesException, ClientIntrouvableException {
        if (requete == null || requete.getId() == null || requete.getId() <= 0) {
            throw DonneesInvalidesException.pourMessage(
                    "L'identifiant du client est obligatoire et doit être un entier strictement positif");
        }

        // Toutes les violations sont collectées puis renvoyées d'un seul tenant :
        // le client SOAP reçoit la liste complète des corrections à apporter.
        List<String> erreurs = new ArrayList<>();
        String nom = texte(requete.getNom(), "nom", 100, erreurs);
        String prenom = texte(requete.getPrenom(), "prénom", 100, erreurs);
        String email = texte(requete.getEmail(), "email", 150, erreurs);
        String telephone = texte(requete.getTelephone(), "téléphone", 20, erreurs);
        String ville = texte(requete.getVille(), "ville", 80, erreurs);
        String pays = texte(requete.getPays(), "pays", 80, erreurs);
        String statut = texte(requete.getStatut(), "statut", 10, erreurs);

        if (email != null && !FORMAT_EMAIL.matcher(email).matches()) {
            erreurs.add("email : le format '" + email + "' n'est pas une adresse valide");
        }
        if (statut != null && !STATUTS_ADMIS.contains(statut.toUpperCase(Locale.ROOT))) {
            erreurs.add("statut : valeur '" + statut + "' non admise (ACTIF ou INACTIF attendu)");
        }
        if (!erreurs.isEmpty()) {
            throw DonneesInvalidesException.pourErreurs(erreurs);
        }

        Client client = repository.findById(requete.getId())
                .orElseThrow(() -> ClientIntrouvableException.pourId(requete.getId()));

        if (email != null) {
            boolean emailDejaPris = repository
                    .findByEmailIgnoreCaseAndIdNot(email, requete.getId())
                    .isPresent();
            if (emailDejaPris) {
                throw DonneesInvalidesException.pourMessage(
                        "email : l'adresse '" + email + "' est déjà utilisée par un autre client");
            }
            client.setEmail(email);
        }
        if (nom != null) {
            client.setNom(nom);
        }
        if (prenom != null) {
            client.setPrenom(prenom);
        }
        if (telephone != null) {
            client.setTelephone(telephone);
        }
        if (ville != null) {
            client.setVille(ville);
        }
        if (pays != null) {
            client.setPays(pays);
        }
        if (statut != null) {
            client.setStatut(statut.toUpperCase(Locale.ROOT));
        }
        return repository.save(client);
    }

    /**
     * Valide un champ texte optionnel : {@code null} = inchangé (retour {@code null}) ;
     * une valeur fournie est trimée, ne peut pas être vide et sa longueur est bornée.
     *
     * @return la valeur nettoyée, ou {@code null} si le champ n'est pas fourni
     */
    private static String texte(String valeur, String libelle, int longueurMax,
                                List<String> erreurs) {
        if (valeur == null) {
            return null;
        }
        String nettoyee = valeur.trim();
        if (nettoyee.isEmpty()) {
            erreurs.add(libelle + " : une valeur vide n'est pas admise (omettre le champ pour laisser inchangé)");
            return null;
        }
        if (nettoyee.length() > longueurMax) {
            erreurs.add(libelle + " : " + nettoyee.length() + " caractères, maximum " + longueurMax);
            return null;
        }
        return nettoyee;
    }
}
