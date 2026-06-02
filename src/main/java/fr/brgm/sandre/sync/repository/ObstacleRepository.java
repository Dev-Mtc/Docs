package fr.brgm.sandre.sync.repository;

import fr.brgm.sandre.sync.domain.Obstacle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObstacleRepository extends JpaRepository<Obstacle, String> {
}
