package com.matera.bootcamp.digitalbank.dto.request;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.br.CPF;

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
public class ClienteRequestDTO {

	@NotBlank
	@Size(max = 100)
	private String nome;
	
	@NotBlank
	@Size(min = 11, max = 11)
	@CPF
	private String cpf;
	
	@NotNull
	@Digits(integer = 12, fraction = 0)
	private Long telefone;
	
	@NotNull
	@Digits(integer = 18, fraction = 2)
	@Range(min = 1000)
	@Positive
	private BigDecimal rendaMensal;
	
	@NotBlank
	@Size(max = 100)
	private String logradouro;
	
	@NotNull
	@Digits(integer = 5, fraction = 0)
	@Positive
	private Integer numero;
	
	@Size(max = 100)
	private String complemento;
	
	@NotBlank
	@Size(max = 100)
	private String bairro;
	
	@NotBlank
	@Size(max = 100)
	private String cidade;
	
	@NotBlank
	@Size(min = 2, max = 2)
	private String estado;
	
	@NotBlank
	@Size(min = 8, max = 8)
	private String cep;

}
