package br.com.fiap.kuravet.repository;

import br.com.fiap.kuravet.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByTutorIdTutor(Long idTutor);
}
