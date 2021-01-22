package com.matera.bootcamp.digitalbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matera.bootcamp.digitalbank.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
