package com.matera.bootcamp.digitalbank;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.repository.ClienteRepository;

@Component
public class AppStartupRunner implements ApplicationRunner {

	ClienteRepository clienteRepository;

	public AppStartupRunner(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Cliente cliente = new Cliente("João", "10399505903", 9999L, new BigDecimal("1000"), "Logradouro", 10,
				"Complemento", "Bairro", "Cidade", "PR", "Cep", null);
		
		clienteRepository.save(cliente);
		
		Cliente cliente2 = clienteRepository.findByCpf("10399505903").orElse(null);
		
		System.out.println("Cliente 2: " + cliente2);
		
		Cliente cliente3 = clienteRepository.buscaPorCpf("10399505903").orElse(null);
		
		System.out.println("Cliente 3: " + cliente3);
		
		Cliente cliente4 = clienteRepository.buscaPorCpfNativeQuery("10399505903").orElse(null);
		
		System.out.println("Cliente 4: " + cliente4);
		
		Cliente cliente5 = clienteRepository.buscaPorCpfENome("10399505903", "João").orElse(null);
		
		System.out.println("Cliente 5: " + cliente5);
		
		Cliente cliente6 = clienteRepository.findByCpfAndNome("10399505903", "João").orElse(null);
		
		System.out.println("Cliente 6: " + cliente6);
		
	}

}
