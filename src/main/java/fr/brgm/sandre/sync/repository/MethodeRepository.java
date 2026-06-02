package fr.brgm.sandre.sync.repository;

import fr.brgm.sandre.sync.domain.Methode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MethodeRepository extends JpaRepository<Methode, String> {
}
