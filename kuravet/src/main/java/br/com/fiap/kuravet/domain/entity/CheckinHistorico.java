package br.com.fiap.kuravet.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_checkin_historico")
public class CheckinHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_checkin")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @Column(name = "dt_checkin", nullable = false, updatable = false)
    private LocalDateTime dataCheckin;

    @Column(name = "ds_status_vitalidade", nullable = false, length = 100)
    private String statusVitalidade;

    @Column(name = "url_foto_pet", length = 255)
    private String urlFotoPet;

    @Column(name = "ds_observacao", length = 500)
    private String observacao;

    @PrePersist
    public void prePersist() {
        this.dataCheckin = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    public LocalDateTime getDataCheckin() { return dataCheckin; }
    public void setDataCheckin(LocalDateTime dataCheckin) { this.dataCheckin = dataCheckin; }
    public String getStatusVitalidade() { return statusVitalidade; }
    public void setStatusVitalidade(String statusVitalidade) { this.statusVitalidade = statusVitalidade; }
    public String getUrlFotoPet() { return urlFotoPet; }
    public void setUrlFotoPet(String urlFotoPet) { this.urlFotoPet = urlFotoPet; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}