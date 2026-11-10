package ma.eventpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administrateurs")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrateur extends User {

}
