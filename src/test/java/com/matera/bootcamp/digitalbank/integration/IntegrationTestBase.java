package com.matera.bootcamp.digitalbank.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import com.matera.bootcamp.digitalbank.repository.ClienteRepository;
import com.matera.bootcamp.digitalbank.repository.ContaRepository;
import com.matera.bootcamp.digitalbank.repository.EstornoRepository;
import com.matera.bootcamp.digitalbank.repository.LancamentoRepository;
import com.matera.bootcamp.digitalbank.repository.TransferenciaRepository;

import io.restassured.RestAssured;

public class IntegrationTestBase {

    @Autowired
    protected ClienteRepository clienteRepository;

    @Autowired
    protected ContaRepository contaRepository;

    @Autowired
    protected EstornoRepository estornoRepository;

    @Autowired
    protected LancamentoRepository lancamentoRepository;

    @Autowired
    protected TransferenciaRepository transferenciaRepository;
    
    @LocalServerPort
    private int port;

    @BeforeEach
    public void limpaBase() {
    	//Informa ao RestAssured em qual porta (aleatória) o contexto do Spring subiu para o teste de integração
    	RestAssured.port = port;
    	
        transferenciaRepository.deleteAll();
        estornoRepository.deleteAll();
        lancamentoRepository.deleteAll();
        contaRepository.deleteAll();
        clienteRepository.deleteAll();
    }

}
