package com.matera.bootcamp.digitalbank.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "db_conta")
public class Conta extends EntidadeBase {

	@Column(precision = 4, nullable = false)
	private Integer numeroAgencia;

	@Column(precision = 12, nullable = false)
	private Long numeroConta;

	@Column(precision = 20, scale = 2, nullable = false)
	private BigDecimal saldo;

	@Column(length = 1, nullable = false)
    private String situacao;
	
	@OneToOne
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	@OneToMany(mappedBy = "conta")
	private List<Lancamento> lancamentos;

}
