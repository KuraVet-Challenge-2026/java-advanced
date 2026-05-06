package br.com.fiap.kuravet.domain.repository;

import br.com.fiap.kuravet.domain.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
}