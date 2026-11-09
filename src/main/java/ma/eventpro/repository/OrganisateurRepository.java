package ma.eventpro.repository;

import ma.eventpro.entity.Organisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganisateurRepository extends JpaRepository<Organisateur , UUID> {
}
