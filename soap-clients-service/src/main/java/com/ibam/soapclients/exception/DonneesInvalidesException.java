package com.ibam.soapclients.exception;

import com.ibam.soapclients.ws.dto.ServiceFault;

import java.util.List;

/**
 * Erreur SOAP « données invalides » — levée lorsqu'un paramètre viole une règle
 * de validation (identifiant absent, e-mail mal formé, statut hors énumération…).
 * Le détail champ par champ est porté par {@link ServiceFault#getErreurs()}.
 */
@jakarta.xml.ws.WebFault(name = "serviceFault", targetNamespace = DonneesInvalidesException.NAMESPACE)
public class DonneesInvalidesException extends Exception {

    public static final String NAMESPACE = "http://ws.soapclients.ibam.com/";
    public static final String CODE = "DONNEES_INVALIDES";

    private final ServiceFault fault;

    public DonneesInvalidesException(ServiceFault fault) {
        super(fault.getMessage());
        this.fault = fault;
    }

    public static DonneesInvalidesException pourMessage(String message) {
        return new DonneesInvalidesException(new ServiceFault(CODE, message, List.of(message)));
    }

    public static DonneesInvalidesException pourErreurs(List<String> erreurs) {
        return new DonneesInvalidesException(new ServiceFault(
                CODE, "La requête contient " + erreurs.size() + " erreur(s) de validation", erreurs));
    }

    /** Bean sérialisé dans le bloc {@code <detail>} de la Fault SOAP. */
    public ServiceFault getFaultInfo() {
        return fault;
    }
}
