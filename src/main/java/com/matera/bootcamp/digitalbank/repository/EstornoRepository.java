package com.matera.bootcamp.digitalbank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matera.bootcamp.digitalbank.entity.Estorno;

public interface EstornoRepository extends JpaRepository<Estorno, Long> {

	Optional<Estorno> findByLancamentoOriginal_Id(Long id);

}
