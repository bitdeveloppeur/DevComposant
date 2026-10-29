package com.ibam.soapclients.config;

import com.ibam.soapclients.ws.ClientServiceWsImpl;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Publication de l'endpoint SOAP par Apache CXF, avec durcissement du parseur
 * XML côté requêtes entrantes (sécurité OWASP : XXE et bombes XML).
 */
@Configuration
public class CxfConfig {

    /** Chemin de l'endpoint, relatif à la racine CXF : {@code /services/clients}. */
    public static final String CHEMIN_ENDPOINT = "/clients";

    @Bean
    public Endpoint endpointClients(Bus bus, ClientServiceWsImpl implementation) {
        EndpointImpl endpoint = new EndpointImpl(bus, implementation);

        Map<String, Object> proprietes = new HashMap<>();
        // Interdit les parseurs « non restreints » : désactive DTD et entités externes
        // (prévention XXE) et empêche toute relâche ultérieure de ces limites.
        proprietes.put("org.apache.cxf.stax.allowInsecureParser", Boolean.FALSE);
        // Limites anti-déni de service (« bombe XML », payloads massifs).
        proprietes.put("org.apache.cxf.stax.maxAttributeCount", "64");
        proprietes.put("org.apache.cxf.stax.maxAttributeSize", "128");
        proprietes.put("org.apache.cxf.stax.maxChildElements", "512");
        proprietes.put("org.apache.cxf.stax.maxElementCount", "2048");
        proprietes.put("org.apache.cxf.stax.maxElementDepth", "64");
        proprietes.put("org.apache.cxf.stax.maxTextLength", "65536");
        endpoint.setProperties(proprietes);

        endpoint.publish(CHEMIN_ENDPOINT);
        return endpoint;
    }
}
