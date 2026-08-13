package br.com.fiap.kuravet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Mapeamento da tabela TUTOR (V1__Criar_Tabelas_Core.sql).
 */
@Entity
@Table(name = "TUTOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tutor_seq")
    @SequenceGenerator(name = "tutor_seq", sequenceName = "SEQ_TUTOR", allocationSize = 1)
    @Column(name = "ID_TUTOR")
    private Long idTutor;

    @Column(name = "NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "CPF", length = 14, nullable = false, unique = true)
    private String cpf;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "ENDERECO", length = 200)
    private String endereco;

    @Builder.Default
    @Column(name = "DATA_CADASTRO", nullable = false)
    private LocalDate dataCadastro = LocalDate.now();
}
