package br.com.fiap.kuravet.repository;

import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPetTutorIdTutor(Long idTutor);

    List<Consulta> findByStatusOrderByDataConsultaAsc(StatusConsulta status);

    List<Consulta> findByPetTutorIdTutorAndStatusOrderByDataConsultaAsc(Long idTutor, StatusConsulta status);

    long countByStatus(StatusConsulta status);
    boolean existsByPetIdPet(Long idPet);


    boolean existsByPetIdPetAndDataConsultaAndStatusIn(Long idPet,
                                                       LocalDate dataConsulta,
                                                       Collection<StatusConsulta> status);
}