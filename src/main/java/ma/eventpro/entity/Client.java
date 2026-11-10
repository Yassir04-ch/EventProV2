package ma.eventpro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
<<<<<<< HEAD
=======
import lombok.experimental.SuperBuilder;
>>>>>>> 6b71b110d2d3209966e0aa1e770fdc2fbcac682b

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
<<<<<<< HEAD
=======
@SuperBuilder
>>>>>>> 6b71b110d2d3209966e0aa1e770fdc2fbcac682b
public class Client extends User {
    @Column
    private String telephone;

    @OneToMany(mappedBy = "client")
    private List<Reservation> reservations = new ArrayList<>();
}
