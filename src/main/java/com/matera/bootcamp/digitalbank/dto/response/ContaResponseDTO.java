package com.matera.bootcamp.digitalbank.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class ContaResponseDTO {

    private Long idCliente;
    private Long idConta;
    private Integer numeroAgencia;
    private Long numeroConta;
    private String situacao;
    private BigDecimal saldo;

}
