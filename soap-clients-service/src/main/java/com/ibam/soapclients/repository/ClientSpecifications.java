package com.ibam.soapclients.repository;

import com.ibam.soapclients.model.Client;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Critères de recherche dynamiques pour {@code searchClients}.
 *
 * <p>Chaque filtre fourni est appliqué ; les filtres absents sont ignorés
 * (pas de clause SQL superflue). Les caractères jokers SQL ({@code %}, {@code _},
 * {@code \}) présents dans la saisie de l'utilisateur sont neutralisés afin que la
 * recherche reste littérale — un motif de recherche ne doit jamais pouvoir détourner
 * le filtre.</p>
 */
public final class ClientSpecifications {

    private static final char CARACTERE_ECHAPPEMENT = '\\';

    private ClientSpecifications() {
    }

    /**
     * Construit la spécification combinant les filtres optionnels.
     *
     * @param nom    fragment de nom (insensible à la casse), peut être {@code null}
     * @param ville  fragment de ville (insensible à la casse), peut être {@code null}
     * @param statut statut exact ({@code ACTIF} / {@code INACTIF}), peut être {@code null}
     */
    public static Specification<Client> recherche(String nom, String ville, String statut) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicats = new ArrayList<>();

            if (StringUtils.hasText(nom)) {
                predicats.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nom")),
                        motifContient(nom), CARACTERE_ECHAPPEMENT));
            }
            if (StringUtils.hasText(ville)) {
                predicats.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("ville")),
                        motifContient(ville), CARACTERE_ECHAPPEMENT));
            }
            if (StringUtils.hasText(statut)) {
                predicats.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("statut")),
                        statut.trim().toUpperCase(Locale.ROOT)));
            }
            return criteriaBuilder.and(predicats.toArray(new Predicate[0]));
        };
    }

    /** Encadre le texte recherché de jokers en neutralisant ceux saisis par l'utilisateur. */
    private static String motifContient(String valeur) {
        String echappe = valeur.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + echappe + "%";
    }
}
