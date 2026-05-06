package br.com.fiap.kuravet.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_evento_consulta")
public class EventoConsulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @Column(name = "tp_evento", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "dt_evento", nullable = false)
    private LocalDate dataEvento;

    @Column(name = "ds_evento", nullable = false, length = 255)
    private String descricaoEvento;

    @Column(name = "nm_veterinario", length = 100)
    private String nomeVeterinario;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }
    public String getDescricaoEvento() { return descricaoEvento; }
    public void setDescricaoEvento(String descricaoEvento) { this.descricaoEvento = descricaoEvento; }
    public String getNomeVeterinario() { return nomeVeterinario; }
    public void setNomeVeterinario(String nomeVeterinario) { this.nomeVeterinario = nomeVeterinario; }
}