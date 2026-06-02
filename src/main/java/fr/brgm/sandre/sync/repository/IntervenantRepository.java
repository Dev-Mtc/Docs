package fr.brgm.sandre.sync.repository;

import fr.brgm.sandre.sync.domain.Intervenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntervenantRepository extends JpaRepository<Intervenant, String> {
}
