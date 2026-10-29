package com.ibam.soapclients.exception;

import com.ibam.soapclients.ws.dto.ServiceFault;

/**
 * Erreur SOAP « erreur interne » — filet de sécurité de la couche WS : toute
 * exception inattendue (base indisponible…) est convertie en Fault générique
 * <em>sans divulguer d'information interne</em> (OWASP : gestion sécurisée des
 * erreurs). Le détail technique est journalisé côté serveur uniquement.
 */
@jakarta.xml.ws.WebFault(name = "serviceFault", targetNamespace = ErreurInterneException.NAMESPACE)
public class ErreurInterneException extends Exception {

    public static final String NAMESPACE = "http://ws.soapclients.ibam.com/";
    public static final String CODE = "ERREUR_INTERNE";
    public static final String MESSAGE_GENERIQUE =
            "Une erreur interne est survenue ; merci de réessayer ultérieurement";

    private final ServiceFault fault;

    public ErreurInterneException(ServiceFault fault) {
        super(fault.getMessage());
        this.fault = fault;
    }

    public static ErreurInterneException generique() {
        return new ErreurInterneException(new ServiceFault(CODE, MESSAGE_GENERIQUE));
    }

    /** Bean sérialisé dans le bloc {@code <detail>} de la Fault SOAP. */
    public ServiceFault getFaultInfo() {
        return fault;
    }
}
