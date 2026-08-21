package br.com.fiap.kuravet.model;

import br.com.fiap.kuravet.enums.Perfil;
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

/**
 * Mapeamento da tabela USUARIO (V1__schema.sql).
 * FK KV_FK_USU_TUTOR -> TUTOR(ID_TUTOR), preenchida somente quando
 * PERFIL = TUTOR (CHECK KV_CK_USU_TUTOR).
 */
@Entity
@Table(name = "USUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "SEQ_USUARIO", allocationSize = 1)
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "USERNAME", length = 60, nullable = false, unique = true)
    private String username;

    @Column(name = "SENHA", length = 100, nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "PERFIL", length = 20, nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TUTOR")
    private Tutor tutor;
}
