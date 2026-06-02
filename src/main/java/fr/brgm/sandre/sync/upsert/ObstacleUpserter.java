package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.domain.Obstacle;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.repository.ObstacleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObstacleUpserter implements ReferentielUpserter {

    private final ObstacleRepository repository;

    public ObstacleUpserter(ObstacleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Referentiel referentiel() {
        return Referentiel.OBSTACLE;
    }

    @Override
    @Transactional
    public int upsert(List<JsonNode> items) {
        List<Obstacle> entities = new ArrayList<>(items.size());
        for (JsonNode item : items) {
            String cd = JsonReader.text(item, "CdObstEcoul");
            if (cd == null) continue;
            Obstacle o = repository.findById(cd).orElseGet(Obstacle::new);
            o.setCdObstEcoul(cd);
            o.setNomPrincipalObstEcoul(JsonReader.text(item, "NomPrincipalObstEcoul"));
            o.setStObstEcoul(JsonReader.text(item, "StObstEcoul"));
            o.setDateValidObstEcoul(JsonReader.dateTime(item, "DateValidObstEcoul"));
            o.setDateMajObstEcoul(JsonReader.dateTime(item, "DateMAJObstEcoul"));
            o.setRawData(item);
            entities.add(o);
        }
        repository.saveAll(entities);
        return entities.size();
    }
}
