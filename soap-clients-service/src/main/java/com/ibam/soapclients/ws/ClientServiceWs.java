package com.ibam.soapclients.ws;

import com.ibam.soapclients.exception.ClientIntrouvableException;
import com.ibam.soapclients.exception.DonneesInvalidesException;
import com.ibam.soapclients.exception.ErreurInterneException;
import com.ibam.soapclients.ws.dto.GetClientDetailsRequest;
import com.ibam.soapclients.ws.dto.GetClientDetailsResponse;
import com.ibam.soapclients.ws.dto.SearchClientsRequest;
import com.ibam.soapclients.ws.dto.SearchClientsResponse;
import com.ibam.soapclients.ws.dto.UpdateClientRequest;
import com.ibam.soapclients.ws.dto.UpdateClientResponse;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Contrat du service SOAP de gestion des clients (Exercice 2, étape 3).
 *
 * <p>Style document/literal <b>bare</b> : chaque opération reçoit un élément
 * requête et renvoie un élément réponse (ex. {@code getClientDetailsRequest} /
 * {@code getClientDetailsResponse}), définis par les classes du package
 * {@code ws.dto}. Le WSDL est publié par Apache CXF sur {@code /services/clients?wsdl}.</p>
 *
 * <p>Opérations :</p>
 * <ul>
 *   <li>{@code getClientDetails} — fiche complète d'un client par identifiant ;</li>
 *   <li>{@code searchClients} — recherche paginée par nom, ville et statut ;</li>
 *   <li>{@code updateClient} — mise à jour partielle (champ absent = inchangé).</li>
 * </ul>
 *
 * <p>Erreurs métier : Faults SOAP {@code serviceFault} (code + message + détail
 * champ par champ) — voir {@code docs/contrat-ws.md}.</p>
 */
@WebService(name = "ClientService", targetNamespace = ClientServiceWs.NAMESPACE)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT,
             use = SOAPBinding.Use.LITERAL,
             parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface ClientServiceWs {

    String NAMESPACE = "http://ws.soapclients.ibam.com/";

    /**
     * Retourne la fiche complète du client identifié par {@code id}.
     *
     * @throws ClientIntrouvableException si aucun client ne porte cet identifiant
     * @throws DonneesInvalidesException  si l'identifiant est absent ou invalide
     */
    @WebMethod(operationName = "getClientDetails")
    GetClientDetailsResponse getClientDetails(
            @WebParam(name = "getClientDetailsRequest", targetNamespace = NAMESPACE)
            GetClientDetailsRequest request)
            throws ClientIntrouvableException, DonneesInvalidesException, ErreurInterneException;

    /**
     * Recherche paginée des clients : filtres optionnels combinés par « ET ».
     * Une recherche sans résultat retourne une liste vide et {@code total = 0}
     * (ce n'est pas une erreur).
     */
    @WebMethod(operationName = "searchClients")
    SearchClientsResponse searchClients(
            @WebParam(name = "searchClientsRequest", targetNamespace = NAMESPACE)
            SearchClientsRequest request)
            throws DonneesInvalidesException, ErreurInterneException;

    /**
     * Met à jour les champs fournis du client identifié par {@code id}
     * (un champ absent de la requête reste inchangé) et retourne l'état final.
     *
     * @throws ClientIntrouvableException si aucun client ne porte cet identifiant
     * @throws DonneesInvalidesException  si un champ fourni viole une règle de validation
     */
    @WebMethod(operationName = "updateClient")
    UpdateClientResponse updateClient(
            @WebParam(name = "updateClientRequest", targetNamespace = NAMESPACE)
            UpdateClientRequest request)
            throws ClientIntrouvableException, DonneesInvalidesException, ErreurInterneException;
}
