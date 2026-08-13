package br.com.fiap.kuravet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Mapeamento da tabela PET (V1__Criar_Tabelas_Core.sql).
 * FK KV_FK_PET_TUTOR -> TUTOR(ID_TUTOR).
 */
@Entity
@Table(name = "PET")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pet_seq")
    @SequenceGenerator(name = "pet_seq", sequenceName = "SEQ_PET", allocationSize = 1)
    @Column(name = "ID_PET")
    private Long idPet;

    @Column(name = "NOME", length = 60, nullable = false)
    private String nome;

    @Column(name = "ESPECIE", length = 30, nullable = false)
    private String especie;

    @Column(name = "RACA", length = 60)
    private String raca;

    @Column(name = "DATA_NASCIMENTO", nullable = false)
    private LocalDate dataNascimento;

    /** CHECK KV_CK_PET_SEXO: 'M' ou 'F'. */
    @Column(name = "SEXO", length = 1, nullable = false)
    private Character sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TUTOR", nullable = false)
    private Tutor tutor;
}
