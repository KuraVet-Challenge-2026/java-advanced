package br.com.fiap.kuravet.model;

import jakarta.persistence.Column;
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

import java.time.LocalDate;

/**
 * Mapeamento da tabela CONSULTA (V1__Criar_Tabelas_Core.sql).
 * FKs KV_FK_CONS_PET -> PET(ID_PET) e KV_FK_CONS_VET -> VETERINARIO(ID_VETERINARIO).
 */
@Entity
@Table(name = "CONSULTA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consulta_seq")
    @SequenceGenerator(name = "consulta_seq", sequenceName = "SEQ_CONSULTA", allocationSize = 1)
    @Column(name = "ID_CONSULTA")
    private Long idConsulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PET", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private Veterinario veterinario;

    @Column(name = "DATA_CONSULTA", nullable = false)
    private LocalDate dataConsulta;

    @Column(name = "TIPO_CONSULTA", length = 40, nullable = false)
    private String tipoConsulta;

    @Column(name = "DIAGNOSTICO", length = 400)
    private String diagnostico;

    /** CHECK KV_CK_CONS_STATUS: AGENDADA | REALIZADA | CANCELADA. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private StatusConsulta status = StatusConsulta.REALIZADA;
}
