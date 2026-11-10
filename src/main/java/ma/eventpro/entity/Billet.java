package ma.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.eventpro.entity.enums.BilletStatus;

import java.time.Instant;

@Entity
@Table(name = "billets")
@Getter
@Setter
@NoArgsConstructor
public class Billet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "reservation_line_id" , nullable = false)
    private LigneReservation lineReservation;

    @Column(nullable = false)
    private Instant issueDate;

    private Instant scanDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BilletStatus status = BilletStatus.VALID;

}
