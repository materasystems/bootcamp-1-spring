package com.matera.bootcamp.digitalbank;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.repository.ClienteRepository;

@Component
public class AppStartupRunner implements ApplicationRunner {

	Printer inglesPrinter;
	Printer portuguesPrinter;

	ClienteRepository clienteRepository;

	public AppStartupRunner(Printer inglesPrinter, Printer portuguesPrinter, ClienteRepository clienteRepository) {
		this.inglesPrinter = inglesPrinter;
		this.portuguesPrinter = portuguesPrinter;
		this.clienteRepository = clienteRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		inglesPrinter.print();
		portuguesPrinter.print();

		// Criamos o registro
		Cliente cliente = new Cliente(null, "João", "10399505903", 9999l, new BigDecimal("1000"), "Logradouro", 10,
				"Complemento", "Bairro", "Cidade", "PR", "Cep");
		Cliente save = clienteRepository.save(cliente);
		System.out.println("Logando o ID Criado: " + save.getId());
		
		// Buscamos o Registro no banco de dados
		Optional<Cliente> findById = clienteRepository.findById(save.getId());
		Cliente clienteRetornado = null;
		if (findById.isPresent()) {
			System.out.println("Busquei o cliente no banco. Nome ");
			clienteRetornado = findById.get();
		} else {
			System.out.println("Não encontrei o cliente.");
		}
		
		// Alteramos o objeto/registro retornado do banco de dados
		clienteRetornado.setNome("Gabriel");
		clienteRepository.save(clienteRetornado);
		
		// Deletando a informação do banco
		clienteRepository.deleteById(clienteRetornado.getId());
		
	}

}
