package com.matera.bootcamp.digitalbank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class ComprovanteResponseDTO {

	private Long idLancamento;
	private String codigoAutenticacao;
	private LocalDateTime dataHora;
	private BigDecimal valor;
	private String natureza;
	private String tipoLancamento;
	private Integer numeroAgencia;
	private Long numeroConta;
	private String descricao;

}
