package com.matera.bootcamp.digitalbank.integration;

import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaClienteRequestDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaRequisicaoDelete;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaRequisicaoGet;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaRequisicaoPost;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.request.LancamentoRequestDTO;
import com.matera.bootcamp.digitalbank.dto.request.TransferenciaRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ComprovanteResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Lancamento;
import com.matera.bootcamp.digitalbank.enumerator.Natureza;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;
import com.matera.bootcamp.digitalbank.enumerator.TipoLancamento;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ContaIntegrationTest extends IntegrationTestBase {

    private static final String URL_BASE = "digitalbank/api/v1/contas";

    private ResponseDTO<ContaResponseDTO> contaResponse;

    @BeforeEach
    public void buildCliente() {
        ClienteRequestDTO clienteRequest = criaClienteRequestDTO();

        contaResponse = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteRequest, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});
    }

    @Test
    public void efetuaDeposito() {
        BigDecimal valor = BigDecimal.valueOf(100);
        String descricao = "Depósito";

        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(valor.intValue()))
                        .body("natureza", equalTo(Natureza.CREDITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.DEPOSITO.getCodigo()))
                        .body("descricao", equalTo(descricao));

        assertSaldoConta(contaResponse.getDados().getIdConta(), valor);
    }

    @Test
    public void efetuaDepositoContaNaoEncontrada() {
        efetuaLancamentoComErro("/2/depositar", "DB-3");
    }

    @Test
    public void efetuaDepositoContaBloqueada() {
        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        efetuaLancamentoComErro("/" + contaResponse.getDados().getIdConta() + "/depositar", "DB-15");
    }

    @Test
    public void efetuaSaque() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Saque");

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/sacar", saque, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(saque.getValor().intValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.SAQUE.getCodigo()))
                        .body("descricao", equalTo(saque.getDescricao()));

        assertSaldoConta(contaResponse.getDados().getIdCliente(), saque.getValor());
    }

    @Test
    public void efetuaSaqueContaNaoEncontrada() {
        efetuaLancamentoComErro("/2/sacar", "DB-3");
    }

    @Test
    public void efetuaSaqueContaBloqueada() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        efetuaLancamentoComErro("/" + contaResponse.getDados().getIdConta() + "/sacar", "DB-15");
    }

    @Test
    public void efetuaSaqueContaSemSaldo() {
        efetuaLancamentoComErro("/" + contaResponse.getDados().getIdConta() + "/sacar", "DB-6");
    }

    @Test
    public void efetuaPagamento() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(50));
        lancamento.setDescricao("Pagamento");

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/pagar", lancamento, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(lancamento.getValor().intValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.PAGAMENTO.getCodigo()))
                        .body("descricao", equalTo(lancamento.getDescricao()));

        assertSaldoConta(contaResponse.getDados().getIdCliente(), lancamento.getValor());
    }

    @Test
    public void efetuaPagamentoContaNaoEncontrada() {
        efetuaLancamentoComErro("/2/pagar", "DB-3");
    }

    @Test
    public void efetuaPagamentoContaBloqueada() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        efetuaLancamentoComErro("/" + contaResponse.getDados().getIdConta() + "/pagar", "DB-15");
    }

    @Test
    public void efetuaPagamentoContaSemSaldo() {
        efetuaLancamentoComErro("/" + contaResponse.getDados().getIdConta() + "/pagar", "DB-6");
    }

    @Test
    public void efetuaTransferencia() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(transferencia.getValor().intValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.TRANSFERENCIA.getCodigo()))
                        .body("descricao", equalTo(transferencia.getDescricao()));

        assertSaldoConta(contaResponse.getDados().getIdCliente(), BigDecimal.valueOf(70));
        assertSaldoConta(contaDestino.getDados().getIdCliente(), BigDecimal.valueOf(30));
    }

    @Test
    public void efetuaTransferenciaContaDebitoNaoEncontrada() {
        efetuaTransferenciaComErro(1, 2L, "/2/transferir", "DB-3");
    }

    @Test
    public void efetuaTransferenciaContaCreditoNaoEncontrada() {
        efetuaTransferenciaComErro(1, 2L, "/" + contaResponse.getDados().getIdConta() + "/transferir", "DB-5");
    }

    @Test
    public void efetuaTransferenciaContaDebitoBloqueada() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        efetuaTransferenciaComErro(contaDestino.getDados().getNumeroAgencia(), contaDestino.getDados().getNumeroConta(), "/" + contaResponse.getDados().getIdConta() + "/transferir", "DB-15");
    }

    @Test
    public void efetuaTransferenciaContaDebitoSemSaldo() {
        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        efetuaTransferenciaComErro(contaDestino.getDados().getNumeroAgencia(), contaDestino.getDados().getNumeroConta(), "/" + contaResponse.getDados().getIdConta() + "/transferir", "DB-6");
    }

    @Test
    public void efetuaTransferenciaContaCreditoBloqueada() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        bloqueiaConta(contaDestino.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        efetuaTransferenciaComErro(contaDestino.getDados().getNumeroAgencia(), contaDestino.getDados().getNumeroConta(), "/" + contaResponse.getDados().getIdConta() + "/transferir", "DB-15");
    }

    @Test
    public void bloqueiaConta() {
        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        assertSituacaoConta(contaResponse.getDados().getIdConta(), SituacaoConta.BLOQUEADA.getCodigo());
    }

    @Test
    public void bloqueiaContaNaoEncontrada() {
        bloqueiaConta(2L, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-3"));
    }

    @Test
    public void bloqueiaContaJaBloqueada() {
        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-13"));
    }

    @Test
    public void desbloqueiaConta() {
        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        desbloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        assertSituacaoConta(contaResponse.getDados().getIdConta(), SituacaoConta.ABERTA.getCodigo());
    }

    @Test
    public void desbloqueiaContaNaoEncontrada() {
        desbloqueiaConta(2L, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-3"));
    }

    @Test
    public void desbloqueiaContaJaBloqueada() {
        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        desbloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        desbloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-14"));
    }

    @Test
    public void estornaLancamentoDeposito() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(comprovante.getDados().getValor().floatValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.ESTORNO.getCodigo()))
                        .body("descricao", equalTo("Estorno do lançamento " + comprovante.getDados().getIdLancamento()));

        assertSaldoConta(contaResponse.getDados().getIdConta(), BigDecimal.ZERO);
    }

    @Test
    public void estornaLancamentoDepositoEstorno() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteEstorno = criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecificationEstorno = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteEstorno.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecificationEstorno, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-8"));
    }

    @Test
    public void estornaLancamentoDepositoJaEstornado() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK);

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-9"));
    }

    @Test
    public void estornaLancamentoDepositoContaBloqueada() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-15"));
    }

    @Test
    public void estornaLancamentoDepositoContaSemSaldo() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(100));
        lancamento.setDescricao("Pagamento");

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/pagar", lancamento, HttpStatus.OK);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-11"));
    }

    @Test
    public void estornaLancamentoSaque() {
        estornaLancamentoDebito("/sacar");
    }

    @Test
    public void estornaLancamentoSaqueEstorno() {
        estornaLancamentoDebitoEstorno("/sacar");
    }

    @Test
    public void estornaLancamentoSaqueJaEstornado() {
        estornaLancamentoDebitoJaEstornado("/sacar");
    }

    @Test
    public void estornaLancamentoSaqueContaBloqueada() {
        estornaLancamentoDebitoContaBloqueada("/sacar");
    }

    @Test
    public void estornaLancamentoPagamento() {
        estornaLancamentoDebito("/pagar");
    }

    @Test
    public void estornaLancamentoPagamentoEstorno() {
        estornaLancamentoDebitoEstorno("/pagar");
    }

    @Test
    public void estornaLancamentoPagamentoJaEstornado() {
        estornaLancamentoDebitoJaEstornado("/pagar");
    }

    @Test
    public void estornaLancamentoPagamentoContaBloqueada() {
        estornaLancamentoDebitoContaBloqueada("/pagar");
    }

    @Test
    public void estornaLancamentoNaoEncontrado() {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", 1)
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-7"));
    }

    @Test
    public void estornaLancamentoTransferencia() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento() + 1L)
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(transferencia.getValor().floatValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.ESTORNO.getCodigo()))
                        .body("descricao", equalTo("Estorno do lançamento " + (comprovante.getDados().getIdLancamento() + 1L)));

        assertSaldoConta(contaResponse.getDados().getIdCliente(), BigDecimal.valueOf(100));
        assertSaldoConta(contaDestino.getDados().getIdCliente(), BigDecimal.ZERO);
    }

    @Test
    public void estornaLancamentoTransferenciaEstorno() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento() + 1L)
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteEstorno = criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecificationEstorno = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteEstorno.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecificationEstorno, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-8"));
    }

    @Test
    public void estornaLancamentoTransferenciaJaEstornado() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento() + 1L)
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK);

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-9"));
    }

    @Test
    public void estornaLancamentoTransferenciaContaDebitada() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-10"));
    }

    @Test
    public void estornaLancamentoTransferenciaContaBloqueada() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento() + 1L)
                    .build();

        bloqueiaConta(contaDestino.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-15"));
    }

    @Test
    public void estornaLancamentoTransferenciaContaSemSaldo() {
        ResponseDTO<ComprovanteResponseDTO> comprovanteDeposito = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK);

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(30));
        lancamento.setDescricao("Pagamento");

        criaRequisicaoPost(URL_BASE + "/" + contaDestino.getDados().getIdConta() + "/pagar", lancamento, HttpStatus.OK);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteDeposito.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-11"));
    }

    @Test
    public void removeLancamentoEstornoDeposito() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteEstorno = criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecificationEstorno = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteEstorno.getDados().getIdLancamento())
                    .build();

        criaRequisicaoDelete(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecificationEstorno, HttpStatus.NO_CONTENT);

        criaRequisicaoGet(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecificationEstorno, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-7"));
    }

    @Test
    public void removeLancamentoOriginalDeposito() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoDelete(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-16"));
    }

    @Test
    public void removeLancamentoEstornoSaque() {
        removeLancamentoDebito("/sacar");
    }

    @Test
    public void removeLancamentoOriginalSaque() {
        removeLancamentoOriginalDebito("/sacar");
    }

    @Test
    public void removeLancamentoEstornoPagamento() {
        removeLancamentoDebito("/pagar");
    }

    @Test
    public void removeLancamentoOriginalPagamento() {
        removeLancamentoOriginalDebito("/pagar");
    }

    @Test
    public void removeLancamentoEstornoTransferencia() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento() + 1L)
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteEstorno = criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecificationEstorno = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteEstorno.getDados().getIdLancamento())
                    .build();

        criaRequisicaoDelete(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecificationEstorno, HttpStatus.BAD_REQUEST)
        			.body("erros", hasSize(1))
    				.body("erros[0].mensagem", containsString("DB-17"));
    }

    @Test
    public void removeLancamentoOriginalTransferencia() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
                .extract()
                    .body()
                        .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaDestino.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento() + 1L)
                    .build();

        criaRequisicaoDelete(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-16"));
    }

    @Test
    public void consultaComprovanteDeposito() {
        ResponseDTO<ComprovanteResponseDTO> comprovante = efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoGet(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", equalTo(comprovante.getDados().getIdLancamento().intValue()))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(comprovante.getDados().getValor().floatValue()))
                        .body("natureza", equalTo(Natureza.CREDITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.DEPOSITO.getCodigo()))
                        .body("descricao", equalTo(comprovante.getDados().getDescricao()));
    }

    @Test
    public void consultaComprovanteSaque() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/sacar", saque, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoGet(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", equalTo(comprovante.getDados().getIdLancamento().intValue()))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(comprovante.getDados().getValor().floatValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.SAQUE.getCodigo()))
                        .body("descricao", equalTo(comprovante.getDados().getDescricao()));
    }

    @Test
    public void consultaComprovantePagamento() {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Pagamento");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/pagar", saque, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoGet(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", equalTo(comprovante.getDados().getIdLancamento().intValue()))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(comprovante.getDados().getValor().floatValue()))
                        .body("natureza", equalTo(Natureza.DEBITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.PAGAMENTO.getCodigo()))
                        .body("descricao", equalTo(comprovante.getDados().getDescricao()));
    }

    @Test
    public void consultaExtratoCompleto() {
        efetuaDeposito(BigDecimal.valueOf(200), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Saque");

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/sacar", saque, HttpStatus.OK);

        LancamentoRequestDTO pagamento = new LancamentoRequestDTO();
        pagamento.setValor(BigDecimal.valueOf(50));
        pagamento.setDescricao("Pagamento");

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/pagar", pagamento, HttpStatus.OK);

        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("id", contaResponse.getDados().getIdConta())
                    .build();

        criaRequisicaoGet(URL_BASE + "/{id}/lancamentos", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("conta.idConta", equalTo(contaResponse.getDados().getIdConta().intValue()))
                        .body("lancamentos", hasSize(4));
    }

    @Test
    public void consultaExtratoPorPeriodo() {
        LocalDateTime dataLancamento = LocalDateTime.now();

        // efetua um depósito
        efetuaDeposito(BigDecimal.valueOf(200), "Depósito", HttpStatus.OK);

        // efetua um saque
        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovanteSaque = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/sacar", saque, HttpStatus.OK)
            .extract()
            .body()
                .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        // modifica a data do lançamento de saque
        Lancamento lancamentoSaque = lancamentoRepository.findById(comprovanteSaque.getDados().getIdLancamento()).get();
        lancamentoSaque.setDataHora(dataLancamento.plusDays(1));
        lancamentoRepository.save(lancamentoSaque);

        // efetua um pagamento
        LancamentoRequestDTO pagamento = new LancamentoRequestDTO();
        pagamento.setValor(BigDecimal.valueOf(50));
        pagamento.setDescricao("Pagamento");

        ResponseDTO<ComprovanteResponseDTO> comprovantePagamento = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/pagar", pagamento, HttpStatus.OK)
            .extract()
            .body()
                .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        // modifica a data do lançamento de pagamento
        Lancamento lancamentoPagamento = lancamentoRepository.findById(comprovantePagamento.getDados().getIdLancamento()).get();
        lancamentoPagamento.setDataHora(dataLancamento.plusDays(2));
        lancamentoRepository.save(lancamentoPagamento);

        // efetua uma transferência
        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        ResponseDTO<ContaResponseDTO> contaDestino = criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ContaResponseDTO>>() {});

        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(contaDestino.getDados().getNumeroAgencia())
                        .numeroConta(contaDestino.getDados().getNumeroConta())
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteTransferencia = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/transferir", transferencia, HttpStatus.OK)
            .extract()
            .body()
                .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        // modifica a data do lançamento de transferência
        Lancamento lancamentoTransferencia = lancamentoRepository.findById(comprovanteTransferencia.getDados().getIdLancamento()).get();
        lancamentoTransferencia.setDataHora(dataLancamento.plusDays(3));
        lancamentoRepository.save(lancamentoTransferencia);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("id", contaResponse.getDados().getIdConta())
                    .addParam("dataInicial", dataLancamento.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    .addParam("dataFinal", dataLancamento.plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    .build();

        criaRequisicaoGet(URL_BASE + "/{id}/lancamentos", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("conta.idConta", equalTo(contaResponse.getDados().getIdConta().intValue()))
                        .body("lancamentos", hasSize(2));
    }

    @Test
    public void consultaTodasContas() {
        ClienteRequestDTO clienteDestino = criaClienteRequestDTO();
        clienteDestino.setCpf("57573694695");
        clienteDestino.setTelefone(997242244L);

        criaRequisicaoPost("digitalbank/api/v1/clientes", clienteDestino, HttpStatus.CREATED);

        criaRequisicaoGet(URL_BASE, HttpStatus.OK)
                    .body("dados", hasSize(2));
    }

    private ValidatableResponse efetuaDeposito(BigDecimal valor, String descricao, HttpStatus httpStatus) {
        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(valor);
        lancamento.setDescricao(descricao);

        return criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + "/depositar", lancamento, httpStatus);
    }

    private ValidatableResponse bloqueiaConta(Long idConta, HttpStatus httpStatus) {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("id", idConta)
                    .build();

        return criaRequisicaoPost(URL_BASE + "/{id}/bloquear", requestSpecification, httpStatus);
    }

    private ValidatableResponse desbloqueiaConta(Long idConta, HttpStatus httpStatus) {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("id", idConta)
                    .build();

        return criaRequisicaoPost(URL_BASE + "/{id}/desbloquear", requestSpecification, httpStatus);
    }

    private void efetuaLancamentoComErro(String url, String codigoErro) {
        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(50));
        lancamento.setDescricao("Lançamento");

        criaRequisicaoPost(URL_BASE + url, lancamento, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString(codigoErro));
    }

    private void efetuaTransferenciaComErro(Integer numeroAgencia, Long numeroConta, String url, String codigoErro) {
        TransferenciaRequestDTO transferencia = TransferenciaRequestDTO
                    .builder()
                        .numeroAgencia(numeroAgencia)
                        .numeroConta(numeroConta)
                        .valor(BigDecimal.valueOf(30))
                        .descricao("Transferência")
                    .build();

        criaRequisicaoPost(URL_BASE + url, transferencia, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString(codigoErro));
    }

    private void removeLancamentoDebito(String uri) {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + uri, saque, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteEstorno = criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecificationEstorno = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteEstorno.getDados().getIdLancamento())
                    .build();

        criaRequisicaoDelete(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecificationEstorno, HttpStatus.NO_CONTENT);

        criaRequisicaoGet(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecificationEstorno, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-7"));
    }

    private void removeLancamentoOriginalDebito(String uri) {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO saque = new LancamentoRequestDTO();
        saque.setValor(BigDecimal.valueOf(50));
        saque.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + uri, saque, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoDelete(URL_BASE + "/{idConta}/lancamentos/{idLancamento}", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-16"));
    }

    private void estornaLancamentoDebito(String uri) {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(100));
        lancamento.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + uri, lancamento, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .root("dados")
                        .body("idLancamento", greaterThan(0))
                        .body("codigoAutenticacao", notNullValue())
                        .body("dataHora", notNullValue())
                        .body("valor", equalTo(comprovante.getDados().getValor().floatValue()))
                        .body("natureza", equalTo(Natureza.CREDITO.getCodigo()))
                        .body("tipoLancamento", equalTo(TipoLancamento.ESTORNO.getCodigo()))
                        .body("descricao", equalTo("Estorno do lançamento " + comprovante.getDados().getIdLancamento()));

        assertSaldoConta(contaResponse.getDados().getIdConta(), BigDecimal.valueOf(100));
    }

    private void estornaLancamentoDebitoEstorno(String uri) {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(100));
        lancamento.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + uri, lancamento, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        ResponseDTO<ComprovanteResponseDTO> comprovanteEstorno = criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecificationEstorno = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovanteEstorno.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecificationEstorno, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-8"));
    }

    private void estornaLancamentoDebitoJaEstornado(String uri) {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(100));
        lancamento.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + uri, lancamento, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.OK);

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-9"));
    }

    private void estornaLancamentoDebitoContaBloqueada(String uri) {
        efetuaDeposito(BigDecimal.valueOf(100), "Depósito", HttpStatus.OK);

        LancamentoRequestDTO lancamento = new LancamentoRequestDTO();
        lancamento.setValor(BigDecimal.valueOf(100));
        lancamento.setDescricao("Saque");

        ResponseDTO<ComprovanteResponseDTO> comprovante = criaRequisicaoPost(URL_BASE + "/" + contaResponse.getDados().getIdConta() + uri, lancamento, HttpStatus.OK)
                    .extract()
                        .body()
                            .as(new TypeRef<ResponseDTO<ComprovanteResponseDTO>>() {});

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("idConta", contaResponse.getDados().getIdConta())
                    .addPathParam("idLancamento", comprovante.getDados().getIdLancamento())
                    .build();

        bloqueiaConta(contaResponse.getDados().getIdConta(), HttpStatus.NO_CONTENT);

        criaRequisicaoPost(URL_BASE + "/{idConta}/lancamentos/{idLancamento}/estornar", requestSpecification, HttpStatus.BAD_REQUEST)
                    .body("erros", hasSize(1))
                    .body("erros[0].mensagem", containsString("DB-15"));
    }

    private ValidatableResponse consultaConta(Long idConta) {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
                    .addPathParam("id", idConta)
                    .build();

        return criaRequisicaoGet("digitalbank/api/v1/clientes/{id}/conta", requestSpecification, HttpStatus.OK);
    }

    private void assertSaldoConta(Long idConta, BigDecimal valor) {
        consultaConta(idConta)
                    .root("dados")
                        .body("saldo", equalTo(valor.floatValue()));
    }

    private void assertSituacaoConta(Long idConta, String situacao) {
        consultaConta(idConta)
                    .root("dados")
                        .body("situacao", equalTo(situacao));
    }
}
