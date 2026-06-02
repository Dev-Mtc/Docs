package fr.brgm.sandre.sync.repository;

import fr.brgm.sandre.sync.domain.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametreRepository extends JpaRepository<Parametre, String> {
}
