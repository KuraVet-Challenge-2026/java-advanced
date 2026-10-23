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

    // O pet entra no mesmo SELECT para montar o nomePet da resposta sem N+1.
    @EntityGraph(attributePaths = "pet")
    List<Triagem> findByPetTutorIdTutor(Long idTutor);

    @Override
    @EntityGraph(attributePaths = "pet")
    List<Triagem> findAll();

    // Historico de um pet; o tutor entra no mesmo SELECT porque a tela mostra o nome dele.
    @EntityGraph(attributePaths = {"pet", "pet.tutor"})
    List<Triagem> findByPetIdPet(Long idPet);

    long countByUrgenciaAndDataTriagemGreaterThanEqual(Urgencia urgencia, LocalDateTime desde);
}
