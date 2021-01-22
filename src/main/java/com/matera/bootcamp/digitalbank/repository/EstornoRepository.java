package com.matera.bootcamp.digitalbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matera.bootcamp.digitalbank.entity.Estorno;

public interface EstornoRepository extends JpaRepository<Estorno, Long> {

}
