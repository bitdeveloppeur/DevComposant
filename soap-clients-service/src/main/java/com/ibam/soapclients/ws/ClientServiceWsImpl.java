package com.ibam.soapclients.ws;

import com.ibam.soapclients.exception.ClientIntrouvableException;
import com.ibam.soapclients.exception.DonneesInvalidesException;
import com.ibam.soapclients.exception.ErreurInterneException;
import com.ibam.soapclients.model.Client;
import com.ibam.soapclients.service.ClientService;
import com.ibam.soapclients.service.MetriquesService;
import com.ibam.soapclients.ws.dto.ClientDetails;
import com.ibam.soapclients.ws.dto.GetClientDetailsRequest;
import com.ibam.soapclients.ws.dto.GetClientDetailsResponse;
import com.ibam.soapclients.ws.dto.SearchClientsRequest;
import com.ibam.soapclients.ws.dto.SearchClientsResponse;
import com.ibam.soapclients.ws.dto.UpdateClientRequest;
import com.ibam.soapclients.ws.dto.UpdateClientResponse;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Implémentation JAX-WS publiée par Apache CXF sur {@code /services/clients?wsdl}.
 *
 * <p>Elle reste volontairement mince (mapping entité &rarr; DTO) : les règles métier
 * vivent dans {@link ClientService} et chaque appel est comptabilisé dans
 * {@link MetriquesService} (succès ou Fault) pour le tableau de bord Grafana.
 * Tout incident inattendu est journalisé côté serveur et renvoyé au client sous
 * forme de Fault générique {@link ErreurInterneException} — aucune information
 * interne ne fuit (OWASP).</p>
 */
@Component
@WebService(serviceName = "ClientService",
        endpointInterface = "com.ibam.soapclients.ws.ClientServiceWs",
        targetNamespace = ClientServiceWs.NAMESPACE)
public class ClientServiceWsImpl implements ClientServiceWs {

    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceWsImpl.class);

    private final ClientService service;
    private final MetriquesService metriques;

    public ClientServiceWsImpl(ClientService service, MetriquesService metriques) {
        this.service = service;
        this.metriques = metriques;
    }

    @Override
    public GetClientDetailsResponse getClientDetails(GetClientDetailsRequest request)
            throws ClientIntrouvableException, DonneesInvalidesException, ErreurInterneException {
        final Integer id = (request == null) ? null : request.getId();
        boolean succes = false;
        try {
            GetClientDetailsResponse reponse = new GetClientDetailsResponse();
            reponse.setClient(versDetails(service.consulter(id)));
            succes = true;
            return reponse;
        } catch (ClientIntrouvableException | DonneesInvalidesException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("getClientDetails({}) : incident inattendu", id, e);
            throw ErreurInterneException.generique();
        } finally {
            metriques.enregistrerAppel("getClientDetails", succes);
        }
    }

    @Override
    public SearchClientsResponse searchClients(SearchClientsRequest request)
            throws DonneesInvalidesException, ErreurInterneException {
        final SearchClientsRequest requete = (request == null) ? new SearchClientsRequest() : request;
        boolean succes = false;
        try {
            Page<Client> resultat = service.rechercher(
                    requete.getNom(), requete.getVille(), requete.getStatut(),
                    requete.getPage(), requete.getTaillePage());

            SearchClientsResponse reponse = new SearchClientsResponse();
            resultat.forEach(client -> reponse.getClients().add(versDetails(client)));
            reponse.setTotal(resultat.getTotalElements());
            reponse.setPage(resultat.getNumber());
            reponse.setTaillePage(resultat.getSize());
            succes = true;
            return reponse;
        } catch (DonneesInvalidesException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("searchClients({}) : incident inattendu", requete, e);
            throw ErreurInterneException.generique();
        } finally {
            metriques.enregistrerAppel("searchClients", succes);
        }
    }

    @Override
    public UpdateClientResponse updateClient(UpdateClientRequest request)
            throws ClientIntrouvableException, DonneesInvalidesException, ErreurInterneException {
        boolean succes = false;
        try {
            UpdateClientResponse reponse = new UpdateClientResponse();
            reponse.setClient(versDetails(service.mettreAJour(request)));
            succes = true;
            return reponse;
        } catch (ClientIntrouvableException | DonneesInvalidesException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("updateClient({}) : incident inattendu",
                    (request == null) ? null : request.getId(), e);
            throw ErreurInterneException.generique();
        } finally {
            metriques.enregistrerAppel("updateClient", succes);
        }
    }

    /** Mappe l'entité JPA vers sa représentation de contrat {@link ClientDetails}. */
    private static ClientDetails versDetails(Client client) {
        ClientDetails details = new ClientDetails();
        details.setId(client.getId());
        details.setNom(client.getNom());
        details.setPrenom(client.getPrenom());
        details.setEmail(client.getEmail());
        details.setTelephone(client.getTelephone());
        details.setVille(client.getVille());
        details.setPays(client.getPays());
        details.setStatut(client.getStatut());
        details.setDateCreation(client.getDateCreation());
        details.setDateModification(client.getDateModification());
        return details;
    }
}
