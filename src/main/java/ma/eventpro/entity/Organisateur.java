package ma.eventpro.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organisateurs")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Organisateur extends User {
    @Column(nullable = false, length = 150)
    private String organisationName;


    @OneToMany(mappedBy = "organisateur")
    @Builder.Default
    private List<Evenement> evenements = new ArrayList<>();
}
