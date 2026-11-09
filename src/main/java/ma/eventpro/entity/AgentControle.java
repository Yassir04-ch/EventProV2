package ma.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "agent_controles")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AgentControle extends User{

}
