package ma.eventpro.repository;

import ma.eventpro.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
=======
>>>>>>> 6b71b110d2d3209966e0aa1e770fdc2fbcac682b

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client , UUID> {

}
