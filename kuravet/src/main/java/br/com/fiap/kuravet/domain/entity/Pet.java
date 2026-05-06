package br.com.fiap.kuravet.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_pet")
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pet")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tutor", nullable = false)
    private Tutor tutor;

    @Column(name = "nm_pet", nullable = false, length = 50)
    private String nome;

    @Column(name = "ds_especie", nullable = false, length = 30)
    private String especie;

    @Column(name = "ds_raca", length = 50)
    private String raca;

    @Column(name = "dt_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "nr_score_vitalidade", nullable = false)
    private Integer scoreVitalidade = 100;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Integer getScoreVitalidade() { return scoreVitalidade; }
    public void setScoreVitalidade(Integer scoreVitalidade) { this.scoreVitalidade = scoreVitalidade; }
}