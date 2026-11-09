package ma.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ligne_reservations")
@Getter
@Setter
@NoArgsConstructor
public class LigneReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal fixedUnitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_billet_id", nullable = false)
    private CategorieBillet categorieBillet;

    @OneToMany(mappedBy = "lineReservation", cascade = CascadeType.ALL)
    private List<Billet> billets = new ArrayList<>();

}
