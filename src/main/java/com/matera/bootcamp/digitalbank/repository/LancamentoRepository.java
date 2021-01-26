package com.matera.bootcamp.digitalbank.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matera.bootcamp.digitalbank.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

	List<Lancamento> findByConta_IdOrderByIdDesc(Long id);

	Optional<Lancamento> findByIdAndConta_Id(Long idLancamento, Long idConta);
	
	@Query("SELECT l " +
		   "FROM   Lancamento l " +
		   "WHERE  l.conta.id = :idConta AND " +
		   "       TRUNC(l.dataHora) BETWEEN :dataInicial AND NVL(:dataFinal, :dataInicial) " +
		   "ORDER BY l.id DESC")
		List<Lancamento> consultaLancamentosPorPeriodo(@Param("idConta") Long idConta, @Param("dataInicial") LocalDate dataInicial, @Param("dataFinal") LocalDate dataFinal);

}
