package com.matera.bootcamp.digitalbank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matera.bootcamp.digitalbank.entity.Transferencia;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

	@Query("SELECT t " +
		   "FROM   Transferencia t " +
		   "WHERE  t.lancamentoDebito.id  = :idLancamento OR " +
		   "       t.lancamentoCredito.id = :idLancamento ")
	Optional<Transferencia> consultaTransferenciaPorIdLancamento(@Param("idLancamento") Long idLancamento);

}
