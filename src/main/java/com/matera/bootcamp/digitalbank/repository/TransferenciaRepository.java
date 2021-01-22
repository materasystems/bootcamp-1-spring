package com.matera.bootcamp.digitalbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matera.bootcamp.digitalbank.entity.Transferencia;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

}
