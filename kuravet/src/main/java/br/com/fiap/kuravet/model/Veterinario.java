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

/**
 * Mapeamento da tabela VETERINARIO (V1__Criar_Tabelas_Core.sql).
 */
@Entity
@Table(name = "VETERINARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "veterinario_seq")
    @SequenceGenerator(name = "veterinario_seq", sequenceName = "SEQ_VETERINARIO", allocationSize = 1)
    @Column(name = "ID_VETERINARIO")
    private Long idVeterinario;

    @Column(name = "NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "CRMV", length = 20, nullable = false, unique = true)
    private String crmv;

    @Column(name = "ESPECIALIDADE", length = 60)
    private String especialidade;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Column(name = "EMAIL", length = 100)
    private String email;
}
