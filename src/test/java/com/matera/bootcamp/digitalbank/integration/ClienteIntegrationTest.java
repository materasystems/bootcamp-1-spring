package com.matera.bootcamp.digitalbank.integration;

import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaClienteRequestDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaRequisicaoGet;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaRequisicaoPost;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaRequisicaoPut;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ResponseDTO;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ClienteIntegrationTest extends IntegrationTestBase {

    private static final String URL_BASE = "digitalbank/api/v1/clientes";

    @Test
    public void cadastraClienteComSucesso() {
        ClienteRequestDTO clienteRequestDTO = criaClienteRequestDTO();

        criaRequisicaoPost(URL_BASE, clienteRequestDTO, HttpStatus.CREATED)
            .root("dados")
                .body("idCliente", greaterThan(0))
                .body("idConta", greaterThan(0))
                .body("numeroAgencia", greaterThan(0))
                .body("numeroConta", equalTo(clienteRequestDTO.getTelefone()))
                .body("situacao", equalTo(SituacaoConta.ABERTA.getCodigo()))
                .body("saldo", equalTo(BigDecimal.ZERO.intValue()));
    }

    @Test
    public void cadastraClienteJaExistente() {
        ClienteRequestDTO cliente = criaClienteRequestDTO();

        criaRequisicaoPost(URL_BASE, cliente, HttpStatus.CREATED);

        ClienteRequestDTO clienteJaExistente = criaClienteRequestDTO();

        criaRequisicaoPost(URL_BASE, clienteJaExistente, HttpStatus.BAD_REQUEST)
            .body("erros", hasSize(1))
            .body("erros[0].mensagem", containsString("DB-2"));
    }

    @Test
    public void consultaClientePorIdSucesso() {
        ClienteRequestDTO cliente = criaClienteRequestDTO();

        ResponseDTO<ContaResponseDTO> response = criaRequisicaoPost(URL_BASE, cliente, HttpStatus.CREATED)
            .extract()
                .body()
                    .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addPathParam("id", response.getDados().getIdCliente())
            .build();

        criaRequisicaoGet(URL_BASE + "/{id}", requestSpecification, HttpStatus.OK);
    }

    @Test
    public void consultaClientePorIdNaoEncontrado() {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addPathParam("id", 1)
            .build();

        criaRequisicaoGet(URL_BASE + "/{id}", requestSpecification, HttpStatus.BAD_REQUEST)
            .body("erros", hasSize(1))
            .body("erros[0].mensagem", containsString("DB-1"));
    }

    @Test
    public void consultaContaPorIdClienteSucesso() {
        ClienteRequestDTO cliente = criaClienteRequestDTO();

        ResponseDTO<ContaResponseDTO> response = criaRequisicaoPost(URL_BASE, cliente, HttpStatus.CREATED)
            .extract()
                .body()
                    .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addPathParam("id", response.getDados().getIdCliente())
            .build();

        criaRequisicaoGet(URL_BASE + "/{id}/conta", requestSpecification, HttpStatus.OK)
            .root("dados")
                .body("idCliente", equalTo(response.getDados().getIdCliente().intValue()))
                .body("idConta", equalTo(response.getDados().getIdConta().intValue()))
                .body("numeroAgencia", equalTo(response.getDados().getNumeroAgencia()))
                .body("numeroConta", equalTo(response.getDados().getNumeroConta()))
                .body("situacao", equalTo(response.getDados().getSituacao()))
                .body("saldo", equalTo(response.getDados().getSaldo().floatValue()));
    }

    @Test
    public void consultaContaPorIdClienteNaoCadastrada() {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addPathParam("id", 1)
            .build();

        criaRequisicaoGet(URL_BASE + "/{id}/conta", requestSpecification, HttpStatus.BAD_REQUEST)
            .body("erros", hasSize(1))
            .body("erros[0].mensagem", containsString("DB-12"));
    }

    @Test
    public void consultaTodosClientesSucesso() {
        ClienteRequestDTO cliente1 = criaClienteRequestDTO();

        ClienteRequestDTO cliente2 = criaClienteRequestDTO();
        cliente2.setCpf("57573694695");
        cliente2.setTelefone(997242244L);

        criaRequisicaoPost(URL_BASE, cliente1, HttpStatus.CREATED);
        criaRequisicaoPost(URL_BASE, cliente2, HttpStatus.CREATED);

        criaRequisicaoGet(URL_BASE, HttpStatus.OK)
            .body("dados", hasSize(2));
    }

    @Test
    public void consultaTodosClientesNaoExisteSucesso() {
        criaRequisicaoGet(URL_BASE, HttpStatus.OK)
            .body("dados", hasSize(0));
    }

    @Test
    public void atualizaClienteSucesso() {
        ClienteRequestDTO cliente = criaClienteRequestDTO();

        ResponseDTO<ContaResponseDTO> response = criaRequisicaoPost(URL_BASE, cliente, HttpStatus.CREATED)
            .extract()
                .body()
                    .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        cliente.setNome("Pedro da Silva");
        cliente.setCpf("57573694695");
        cliente.setTelefone(997242244L);
        cliente.setRendaMensal(BigDecimal.valueOf(5000));
        cliente.setLogradouro("Avenida Paulista");
        cliente.setNumero(100);
        cliente.setComplemento("Casa");
        cliente.setBairro("Paulista");
        cliente.setCidade("São Paulo");
        cliente.setEstado("SP");
        cliente.setCep("73887445");

        criaRequisicaoPut(URL_BASE + "/" + response.getDados().getIdCliente(), cliente, HttpStatus.NO_CONTENT);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addPathParam("id", response.getDados().getIdCliente())
            .build();

        criaRequisicaoGet(URL_BASE + "/{id}", requestSpecification, HttpStatus.OK)
            .root("dados")
                .body("nome", equalTo(cliente.getNome()))
                .body("cpf", equalTo(cliente.getCpf()))
                .body("telefone", equalTo(cliente.getTelefone().intValue()))
                .body("rendaMensal", equalTo(cliente.getRendaMensal().floatValue()))
                .body("logradouro", equalTo(cliente.getLogradouro()))
                .body("numero", equalTo(cliente.getNumero()))
                .body("complemento", equalTo(cliente.getComplemento()))
                .body("bairro", equalTo(cliente.getBairro()))
                .body("cidade", equalTo(cliente.getCidade()))
                .body("estado", equalTo(cliente.getEstado()))
                .body("cep", equalTo(cliente.getCep()));
    }

    @Test
    public void atualizaClienteNaoEncontrado() {
        ClienteRequestDTO cliente = criaClienteRequestDTO();

        criaRequisicaoPut(URL_BASE + "/" + 1, cliente, HttpStatus.BAD_REQUEST)
            .body("erros", hasSize(1))
            .body("erros[0].mensagem", containsString("DB-1"));
    }

}
