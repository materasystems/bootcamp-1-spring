package com.matera.bootcamp.digitalbank.utils;

import java.math.BigDecimal;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;

public class DigitalbankTestUtils {

	public static ContaResponseDTO criaContaResponseDTO() {
		return ContaResponseDTO.builder().idCliente(1L)
						                .idConta(2L)
						                .numeroAgencia(1234)
						                .numeroConta(102030L)
						                .saldo(BigDecimal.ZERO)
						                .situacao(SituacaoConta.ABERTA.getCodigo())
						                .build();
	}

	public static ClienteRequestDTO criaClienteRequestDTO() {
		return ClienteRequestDTO.builder().bairro("Bairro 1")
                .cep("87087087")
                .cidade("Maringá")
                .complemento(null)
                .cpf("01234567890")
                .estado("PR")
                .logradouro("Rua 1")
                .nome("Cliente 1")
                .numero(123)
                .rendaMensal(BigDecimal.valueOf(5000))
                .telefone(91234567L)
                .build();
	}
}
