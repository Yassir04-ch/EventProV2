package ma.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories_billets",
        uniqueConstraints = {@UniqueConstraint(name = "uk_categorie_event_name", columnNames = {"evenement_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
public class CategorieBillet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal prixUnitaire;

    @Column(nullable = false)
    private Integer placesReservees = 0;

    @Column(nullable = false)
    private Integer quantiteTotale;

    @Column(nullable = false)
    private Integer quotaMaxParClient ;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

}
