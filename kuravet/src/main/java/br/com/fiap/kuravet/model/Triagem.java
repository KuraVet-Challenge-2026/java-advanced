package br.com.fiap.kuravet.model;

import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.Sintoma;
import br.com.fiap.kuravet.ia.Urgencia;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TRIAGEM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Triagem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "triagem_seq")
    @SequenceGenerator(name = "triagem_seq", sequenceName = "SEQ_TRIAGEM", allocationSize = 1)
    @Column(name = "ID_TRIAGEM")
    private Long idTriagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PET", nullable = false)
    private Pet pet;

    @Column(name = "RELATO", length = 1000, nullable = false)
    private String relato;

    @Builder.Default
    @Convert(converter = SintomasConverter.class)
    @Column(name = "SINTOMAS", length = 500)
    private Set<Sintoma> sintomas = EnumSet.noneOf(Sintoma.class);

    @Enumerated(EnumType.STRING)
    @Column(name = "URGENCIA", length = 20, nullable = false)
    private Urgencia urgencia;

    @Column(name = "RECOMENDACAO", length = 400, nullable = false)
    private String recomendacao;

    @Builder.Default
    @Convert(converter = RegrasAplicadasConverter.class)
    @Column(name = "REGRAS_APLICADAS", length = 1000, nullable = false)
    private List<String> regrasAplicadas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "METODO_EXTRACAO", length = 20, nullable = false)
    private MetodoExtracao metodoExtracao;

    @Column(name = "DATA_TRIAGEM", nullable = false)
    private LocalDateTime dataTriagem;
}
