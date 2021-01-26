package com.matera.bootcamp.digitalbank;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.service.ClienteService;

@Component
public class AppStartupRunner implements ApplicationRunner {

	private ClienteService clienteService;

	public AppStartupRunner(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// Cadastrando via Service
		ClienteRequestDTO cliente = ClienteRequestDTO.builder().nome("João")
															   .cpf("10399505903")
															   .telefone(9999L)
															   .rendaMensal(new BigDecimal("1000"))
															   .logradouro("Logradouro")
															   .numero(10)
															   .complemento("Complemento")
															   .bairro("Bairro")
															   .cidade("Maringa")
															   .estado("PR")
															   .cep("87046920")
															   .build();
		
		ContaResponseDTO contaSalva = clienteService.cadastra(cliente);
		
		// Consultado por ID via Service
		System.out.println("Conta salva: " + contaSalva);
		System.out.println("Consulta por id: " + clienteService.consulta(contaSalva.getIdCliente()));
		
		// Consultando todos os Clientes
		ClienteRequestDTO cliente2 = ClienteRequestDTO.builder().nome("Gabriel")
															    .cpf("10399505904")
															    .telefone(9998L)
															    .rendaMensal(new BigDecimal("1000"))
															    .logradouro("Logradouro")
															    .numero(10)
															    .complemento("Complemento")
															    .bairro("Bairro")
															    .cidade("Maringa")
															    .estado("PR")
															    .cep("87046920")
															    .build();
		
		ContaResponseDTO conta2Salva = clienteService.cadastra(cliente2);
		
		System.out.println("Conta 2 salva: " + conta2Salva);
		System.out.println("Listar todos: " + clienteService.consultaTodos());
		
		// Atualiza Cliente
		ClienteRequestDTO dadosAlteracao = ClienteRequestDTO.builder().nome("Gabriel")
																	  .cpf("10399505904")
																	  .telefone(9998L)
																	  .rendaMensal(new BigDecimal("2000"))
																	  .logradouro("Logradouro")
																	  .numero(10)
																	  .complemento("Complemento")
																	  .bairro("Bairro")
																	  .cidade("Maringa")
																	  .estado("PR")
																	  .cep("87046920")
																	  .build();
		
		clienteService.atualiza(conta2Salva.getIdCliente(), dadosAlteracao);
		
		System.out.println("Cliente alterado: " + clienteService.consulta(conta2Salva.getIdCliente()));
	}

}
