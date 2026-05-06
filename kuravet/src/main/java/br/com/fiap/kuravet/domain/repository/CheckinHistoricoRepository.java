package br.com.fiap.kuravet.domain.repository;

import br.com.fiap.kuravet.domain.entity.CheckinHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckinHistoricoRepository extends JpaRepository<CheckinHistorico, Long> {
}