package com.matera.bootcamp.digitalbank.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "conta")
@Entity
@Table(name = "db_cliente")
public class Cliente extends EntidadeBase {

	@Column(length = 100, nullable = false)
	private String nome;

	@Column(length = 11, nullable = false, unique = true)
	private String cpf;

	@Column(precision = 12, nullable = false)
	private Long telefone;

	@Column(precision = 20, scale = 2, nullable = false)
	private BigDecimal rendaMensal;

	@Column(length = 100, nullable = false)
	private String logradouro;

	@Column(precision = 5, nullable = false)
	private Integer numero;

	@Column(length = 100, nullable = true)
	private String complemento;

	@Column(length = 100, nullable = false)
	private String bairro;

	@Column(length = 100, nullable = false)
	private String cidade;

	@Column(length = 2, nullable = false)
	private String estado;

	@Column(length = 8, nullable = false)
	private String cep;
	
	@OneToOne(mappedBy = "cliente")
	private Conta conta;

}
