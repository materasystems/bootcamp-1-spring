package com.matera.bootcamp.digitalbank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matera.bootcamp.digitalbank.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

	Optional<Conta> findByNumeroConta(Long telefone);

}
