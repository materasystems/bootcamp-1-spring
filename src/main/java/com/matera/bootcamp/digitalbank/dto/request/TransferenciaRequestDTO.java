package com.matera.bootcamp.digitalbank.dto.request;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferenciaRequestDTO {

	@NotNull
	@Digits(integer = 4, fraction = 0)
	@Positive
	private Integer numeroAgencia;
	
	@NotNull
	@Digits(integer = 12, fraction = 0)
	@Positive
	private Long numeroConta;
	
	@NotNull
	@Digits(integer = 18, fraction = 2)
	@Positive
	private BigDecimal valor;
	
	@Size(max = 50)
	private String descricao;

}
