package br.com.fiap.kuravet.repository;

import br.com.fiap.kuravet.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPetTutorIdTutor(Long idTutor);
}
