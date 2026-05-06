package br.com.fiap.kuravet.domain.repository;

import br.com.fiap.kuravet.domain.entity.EventoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoConsultaRepository extends JpaRepository<EventoConsulta, Long> {
}