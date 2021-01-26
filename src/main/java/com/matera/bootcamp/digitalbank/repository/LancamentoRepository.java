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
	
	@Query("SELECT lan " +
	       "FROM   Lancamento lan " +
		   "WHERE  TRUNC(lan.dataHora) BETWEEN :dataInicial AND NVL(:dataFinal, :dataInicial) " +
	       "ORDER  BY lan.id DESC")
	List<Lancamento> consultaLancamentosPorPeriodo(@Param("dataInicial") LocalDate dataInicial, @Param("dataFinal") LocalDate dataFinal);

}
