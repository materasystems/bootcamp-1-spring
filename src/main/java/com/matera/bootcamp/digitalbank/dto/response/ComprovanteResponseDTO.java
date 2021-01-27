package com.matera.bootcamp.digitalbank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
@JsonInclude(Include.NON_NULL)
@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
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
