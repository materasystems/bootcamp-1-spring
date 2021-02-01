package com.matera.bootcamp.digitalbank.utils;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.request.LancamentoRequestDTO;
import com.matera.bootcamp.digitalbank.dto.request.TransferenciaRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ComprovanteResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.entity.Conta;
import com.matera.bootcamp.digitalbank.entity.Lancamento;
import com.matera.bootcamp.digitalbank.entity.Transferencia;
import com.matera.bootcamp.digitalbank.enumerator.Natureza;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;
import com.matera.bootcamp.digitalbank.enumerator.TipoLancamento;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class DigitalbankTestUtils {

    private DigitalbankTestUtils() {}

    public static ValidatableResponse criaRequisicaoPost(String url, Object body, HttpStatus httpStatus) {
        return given().
                    spec(criaRequestSpecification(body)).
                    log().
                        all().
               when().
                    post(url).
               then().
                    statusCode(httpStatus.value());
    }

    public static ValidatableResponse criaRequisicaoPost(String url, RequestSpecification requestSpecification, HttpStatus httpStatus) {
        return given().
                    spec(requestSpecification).
                    log().
                        all().
               when().
                    post(url).
               then().
                    statusCode(httpStatus.value());
    }

    public static ValidatableResponse criaRequisicaoPut(String url, Object body, HttpStatus httpStatus) {
        return given().
                    spec(criaRequestSpecification(body)).
                    log().
                        all().
               when().
                    put(url).
               then().
                    statusCode(httpStatus.value());
    }

    public static ValidatableResponse criaRequisicaoDelete(String url, RequestSpecification requestSpecification, HttpStatus httpStatus) {
        return given().
                    spec(requestSpecification).
                    log().
                        all().
               when().
                    delete(url).
               then().
                    statusCode(httpStatus.value());
    }

    public static ValidatableResponse criaRequisicaoGet(String url, RequestSpecification requestSpecification, HttpStatus httpStatus) {
        return given().
                    spec(requestSpecification).
                    log().
                        all().
               when().
                    get(url).
               then().
                    statusCode(httpStatus.value());
    }

    public static ValidatableResponse criaRequisicaoGet(String url, HttpStatus httpStatus) {
        return given().
                    log().
                        all().
               when().
                    get(url).
               then().
                    statusCode(httpStatus.value());
    }
    
    public static ClienteRequestDTO criaClienteRequestDTO() {
        return ClienteRequestDTO.builder().bairro("Bairro 1")
                                          .cep("87087087")
                                          .cidade("Maringá")
                                          .complemento("Casa 1")
                                          .cpf("72979929921")
                                          .estado("PR")
                                          .logradouro("Rua 1")
                                          .nome("Cliente 1")
                                          .numero(100)
                                          .rendaMensal(BigDecimal.valueOf(5000))
                                          .telefone(44999001234L)
                                          .build();
    }

    public static Cliente criaClienteEntidade() {
        return Cliente.builder().bairro("Centro")
                                .cep("87005002")
                                .cidade("Maringá")
                                .complemento("Apto 207")
                                .cpf("05728520022")
                                .estado("PR")
                                .id(1L)
                                .logradouro("Avenida São Paulo")
                                .nome("João da Silva")
                                .numero(1287)
                                .rendaMensal(BigDecimal.valueOf(10000))
                                .telefone(997542877L)
                                .build();
    }

    public static Conta criaContaEntidade() {
        return Conta.builder().cliente(criaClienteEntidade())
                              .id(2L)
                              .numeroAgencia(1234)
                              .numeroConta(102030L)
                              .saldo(BigDecimal.valueOf(5000))
                              .situacao(SituacaoConta.ABERTA.getCodigo())
                              .build();
    }

    public static ContaResponseDTO criaContaResponseDTO() {
        return ContaResponseDTO.builder().idCliente(1L)
                                         .idConta(2L)
                                         .numeroAgencia(1234)
                                         .numeroConta(102030L)
                                         .saldo(BigDecimal.ZERO)
                                         .situacao(SituacaoConta.ABERTA.getCodigo())
                                         .build();
    }

    public static LancamentoRequestDTO criaLancamentoRequestDTO(BigDecimal valor) {
        return LancamentoRequestDTO.builder().descricao("Lançamento Teste")
                                             .valor(valor)
                                             .build();
    }

    public static Lancamento criaLancamentoEntidade(TipoLancamento tipoLancamento, Natureza natureza, BigDecimal valor) {
        return Lancamento.builder().id(1L)
                                   .codigoAutenticacao("123456")
                                   .conta(criaContaEntidade())
                                   .dataHora(LocalDateTime.now())
                                   .descricao("Lançamento Teste")
                                   .natureza(natureza.getCodigo())
                                   .tipoLancamento(tipoLancamento.getCodigo())
                                   .valor(valor)
                                   .build();
    }

    public static ComprovanteResponseDTO criaComprovanteResponseDTO() {
        return ComprovanteResponseDTO.builder().codigoAutenticacao("123456")
                                               .dataHora(LocalDateTime.now())
                                               .descricao("Lançamento Teste")
                                               .idLancamento(1L)
                                               .natureza(Natureza.CREDITO.getCodigo())
                                               .numeroAgencia(1)
                                               .numeroConta(12345L)
                                               .tipoLancamento(TipoLancamento.DEPOSITO.getCodigo())
                                               .valor(BigDecimal.valueOf(100))
                                               .build();
    }

    public static TransferenciaRequestDTO criaTransferenciaRequestDTO(Integer numeroAgencia, Long numeroConta, BigDecimal valor) {
        return TransferenciaRequestDTO.builder()
                                      .descricao("Transferência Teste")
                                      .numeroAgencia(numeroAgencia)
                                      .numeroConta(numeroConta)
                                      .valor(valor)
                                      .build();
    }

    public static Transferencia criaTransferenciaEntidade(BigDecimal valor) {
        Lancamento lancamentoDebito = Lancamento.builder().id(1L)
                                                          .codigoAutenticacao("123456")
                                                          .conta(criaContaEntidade())
                                                          .dataHora(LocalDateTime.now())
                                                          .descricao("Transferência Teste 1")
                                                          .natureza(Natureza.DEBITO.getCodigo())
                                                          .tipoLancamento(TipoLancamento.TRANSFERENCIA.getCodigo())
                                                          .valor(valor)
                                                          .build();

        Lancamento lancamentoCredito = Lancamento.builder().id(2L)
                                                           .codigoAutenticacao("123457")
                                                           .conta(criaContaEntidade())
                                                           .dataHora(LocalDateTime.now())
                                                           .descricao("Transferência Teste 2")
                                                           .natureza(Natureza.CREDITO.getCodigo())
                                                           .tipoLancamento(TipoLancamento.TRANSFERENCIA.getCodigo())
                                                           .valor(valor)
                                                           .build();

        return Transferencia.builder().id(1L)
                                      .lancamentoCredito(lancamentoCredito)
                                      .lancamentoDebito(lancamentoDebito)
                                      .build();
    }
    
    private static RequestSpecification criaRequestSpecification(Object body) {
        return new RequestSpecBuilder().setContentType(ContentType.JSON)
                                       .addHeader("Accept", ContentType.JSON.toString())
                                       .setBody(body)
                                       .build();
    }

}
