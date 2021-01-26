package com.matera.bootcamp.digitalbank.dto.request;

import java.math.BigDecimal;

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

	private Integer numeroAgencia;
	private Long numeroConta;
	private BigDecimal valor;
	private String descricao;

}
