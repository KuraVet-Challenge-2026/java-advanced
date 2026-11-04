package br.com.fiap.kuravet.repository;

import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Triagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    @EntityGraph(attributePaths = "pet")
    List<Triagem> findByPetTutorIdTutor(Long idTutor);

    @Override
    @EntityGraph(attributePaths = "pet")
    List<Triagem> findAll();

    @EntityGraph(attributePaths = {"pet", "pet.tutor"})
    List<Triagem> findByPetIdPet(Long idPet);

    List<Triagem> findTop3ByPetIdPetOrderByDataTriagemDesc(Long idPet);

    long countByUrgenciaAndDataTriagemGreaterThanEqual(Urgencia urgencia, LocalDateTime desde);
}
