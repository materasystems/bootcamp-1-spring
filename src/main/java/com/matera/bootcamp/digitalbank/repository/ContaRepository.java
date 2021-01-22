package com.matera.bootcamp.digitalbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matera.bootcamp.digitalbank.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

}
