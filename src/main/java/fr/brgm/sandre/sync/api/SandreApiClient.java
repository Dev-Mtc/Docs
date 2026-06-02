package fr.brgm.sandre.sync.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.brgm.sandre.sync.config.SandreProperties;
import fr.brgm.sandre.sync.domain.Referentiel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Client de l'API REST SANDRE.
 *
 * <p>Implémente la pagination automatique : boucle sur {@code offset} par pas de {@code limit}
 * jusqu'à obtenir une page incomplète (taille &lt; {@code limit}) ou vide (clé d'élément absente).</p>
 */
@Component
public class SandreApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(SandreApiClient.class);

    private final WebClient webClient;
    private final SandreProperties properties;
    private final ObjectMapper objectMapper;

    public SandreApiClient(WebClient sandreWebClient, SandreProperties properties, ObjectMapper objectMapper) {
        this.webClient = sandreWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Récupère l'ensemble des éléments d'un référentiel, en mode complet ou incrémental.
     *
     * @param referentiel       descripteur du référentiel à interroger
     * @param derniereDateDeMAJ date à partir de laquelle ne récupérer que les modifications,
     *                          ou {@link Optional#empty()} pour une synchro complète (premier run)
     * @return tous les éléments collectés à travers les pages, sous forme de {@link JsonNode}
     * @throws SandreApiException en cas d'erreur HTTP, de timeout ou de réponse non parsable
     */
    public List<JsonNode> recuperer(Referentiel referentiel, Optional<LocalDate> derniereDateDeMAJ) {
        List<JsonNode> all = new ArrayList<>();
        int limit = properties.pageSize();
        int offset = 0;
        int page = 0;

        while (true) {
            page++;
            List<JsonNode> items = fetchPage(referentiel, derniereDateDeMAJ, limit, offset);
            all.addAll(items);

            LOG.debug("Référentiel {} : page {} → {} éléments (offset={}, limit={})",
                    referentiel.name(), page, items.size(), offset, limit);

            if (items.size() < limit) {
                break;
            }
            offset += limit;
        }

        return all;
    }

    private List<JsonNode> fetchPage(Referentiel referentiel,
                                     Optional<LocalDate> derniereDateDeMAJ,
                                     int limit,
                                     int offset) {
        Function<UriBuilder, URI> uriBuilder = b -> {
            UriBuilder ub = b.path("/{ref}.json")
                    .queryParam("limit", limit)
                    .queryParam("offset", offset);
            derniereDateDeMAJ.ifPresent(d ->
                    ub.queryParam("derniereDateDeMAJ", d.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            return ub.build(referentiel.urlCode());
        };

        String body;
        try {
            body = webClient.get()
                    .uri(uriBuilder)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> Mono.error(
                            new SandreApiException("Réponse HTTP " + ex.getStatusCode().value()
                                    + " pour le référentiel " + referentiel.urlCode()
                                    + " (offset=" + offset + ", limit=" + limit + ")", ex)))
                    .block(properties.timeout());
        } catch (SandreApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SandreApiException("Échec de l'appel API SANDRE pour le référentiel "
                    + referentiel.urlCode() + " (offset=" + offset + ")", e);
        }

        if (body == null || body.isBlank()) {
            throw new SandreApiException("Corps de réponse vide pour le référentiel " + referentiel.urlCode());
        }

        return extractItems(body, referentiel);
    }

    private List<JsonNode> extractItems(String body, Referentiel referentiel) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new SandreApiException("JSON invalide pour le référentiel " + referentiel.urlCode(), e);
        }

        JsonNode items = root.path("REFERENTIELS").path("Referentiel").path(referentiel.itemWrapperKey());
        if (items.isMissingNode() || items.isNull()) {
            return List.of();
        }

        // L'API renvoie soit un tableau (cas normal multi-éléments), soit un objet unique
        // si la réponse ne contient qu'un seul item. On normalise les deux cas.
        if (items.isObject()) {
            return List.of(items);
        }
        if (!items.isArray()) {
            return List.of();
        }

        List<JsonNode> out = new ArrayList<>(items.size());
        for (Iterator<JsonNode> it = items.elements(); it.hasNext(); ) {
            out.add(it.next());
        }
        return out;
    }
}
