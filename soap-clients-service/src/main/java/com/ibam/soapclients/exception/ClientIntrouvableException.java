package com.ibam.soapclients.exception;

import com.ibam.soapclients.ws.dto.ServiceFault;

/**
 * Erreur SOAP « client introuvable » — levée par {@code getClientDetails} et
 * {@code updateClient} lorsque l'identifiant demandé n'existe pas.
 */
@jakarta.xml.ws.WebFault(name = "serviceFault", targetNamespace = ClientIntrouvableException.NAMESPACE)
public class ClientIntrouvableException extends Exception {

    public static final String NAMESPACE = "http://ws.soapclients.ibam.com/";
    public static final String CODE = "CLIENT_INTROUVABLE";

    private final ServiceFault fault;

    public ClientIntrouvableException(ServiceFault fault) {
        super(fault.getMessage());
        this.fault = fault;
    }

    public static ClientIntrouvableException pourId(Object id) {
        return new ClientIntrouvableException(
                new ServiceFault(CODE, "Aucun client ne correspond à l'identifiant " + id));
    }

    /** Bean sérialisé dans le bloc {@code <detail>} de la Fault SOAP. */
    public ServiceFault getFaultInfo() {
        return fault;
    }
}
