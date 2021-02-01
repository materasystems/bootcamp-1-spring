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
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonInclude(Include.NON_NULL)
public class ComprovanteResponseDTO {

	private Long idLancamento;
	private String codigoAutenticacao;
	
	@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
	private LocalDateTime dataHora;
	
	private BigDecimal valor;
	private String natureza;
	private String tipoLancamento;
	private Integer numeroAgencia;
	private Long numeroConta;
	private String descricao;

}
