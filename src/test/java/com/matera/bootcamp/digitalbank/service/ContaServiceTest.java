package com.matera.bootcamp.digitalbank.service;

import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaClienteEntidade;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaComprovanteResponseDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaContaEntidade;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaLancamentoEntidade;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaLancamentoRequestDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaTransferenciaRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.matera.bootcamp.digitalbank.dto.request.LancamentoRequestDTO;
import com.matera.bootcamp.digitalbank.dto.request.TransferenciaRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ComprovanteResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ExtratoResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.entity.Conta;
import com.matera.bootcamp.digitalbank.entity.Lancamento;
import com.matera.bootcamp.digitalbank.enumerator.Natureza;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;
import com.matera.bootcamp.digitalbank.enumerator.TipoLancamento;
import com.matera.bootcamp.digitalbank.exception.ServiceException;
import com.matera.bootcamp.digitalbank.repository.ContaRepository;

@ExtendWith(MockitoExtension.class)
public class ContaServiceTest {

    private static final Integer NUMERO_MAXIMO_AGENCIA = 5;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private LancamentoService lancamentoService;

    @InjectMocks
    private ContaService contaService;

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(contaService, "numeroMaximoAgencia", NUMERO_MAXIMO_AGENCIA);
    }

    @Test
    public void cadastraContaComSucesso() {
        Cliente cliente = criaClienteEntidade();

        when(contaRepository.findByNumeroConta(eq(cliente.getTelefone()))).thenReturn(Optional.empty());
        when(contaRepository.save(any(Conta.class))).thenReturn(criaContaEntidade());

        ContaResponseDTO contaResponse = contaService.cadastra(cliente);

        verify(contaRepository).findByNumeroConta(eq(cliente.getTelefone()));
        verify(contaRepository).save(any(Conta.class));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertNotNull(contaResponse);
    }

    @Test
    public void cadastraContaJaExistente() {
        Cliente cliente = criaClienteEntidade();
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findByNumeroConta(eq(cliente.getTelefone()))).thenReturn(Optional.of(contaMock));

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.cadastra(cliente));

        verify(contaRepository).findByNumeroConta(eq(cliente.getTelefone()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-4", serviceException.getCodigoErro());
    }

    @Test
    public void efetuaLancamentoCreditoComSucesso() {
        Natureza natureza = Natureza.CREDITO;
        TipoLancamento tipoLancamento = TipoLancamento.DEPOSITO;
        BigDecimal valor = BigDecimal.valueOf(100);

        Conta contaMock = criaContaEntidade();
        BigDecimal saldoContaAntes = contaMock.getSaldo();
        LancamentoRequestDTO lancamentoRequest = criaLancamentoRequestDTO(valor);
        Lancamento lancamentoMock = criaLancamentoEntidade(tipoLancamento, natureza, valor);

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));
        when(lancamentoService.efetuaLancamento(eq(lancamentoRequest), eq(contaMock), eq(natureza), eq(tipoLancamento))).thenReturn(lancamentoMock);
        when(lancamentoService.entidadeParaComprovanteResponseDTO(eq(lancamentoMock))).thenReturn(criaComprovanteResponseDTO());

        ComprovanteResponseDTO comprovanteResponse = contaService.efetuaLancamento(contaMock.getId(), lancamentoRequest, tipoLancamento);

        verify(contaRepository).findById(eq(contaMock.getId()));
        verify(contaRepository).save(eq(contaMock));
        verify(lancamentoService).efetuaLancamento(eq(lancamentoRequest), eq(contaMock), eq(natureza), eq(tipoLancamento));
        verifyNoMoreInteractions(contaRepository);
        verifyNoMoreInteractions(lancamentoService);

        assertEquals(saldoContaAntes.add(valor), contaMock.getSaldo());
        assertNotNull(comprovanteResponse);
    }

    @Test
    public void efetuaLancamentoContaInexistente() {
        TipoLancamento tipoLancamento = TipoLancamento.DEPOSITO;
        BigDecimal valor = BigDecimal.valueOf(100);
        Long idConta = 1L;
        LancamentoRequestDTO lancamentoRequest = criaLancamentoRequestDTO(valor);

        when(contaRepository.findById(eq(idConta))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.efetuaLancamento(idConta, lancamentoRequest, tipoLancamento));

        verify(contaRepository).findById(eq(idConta));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-3", serviceException.getCodigoErro());
    }

    @Test
    public void efetuaTransferenciaComSucesso() {
        TipoLancamento tipoLancamento = TipoLancamento.TRANSFERENCIA;
        BigDecimal valor = BigDecimal.valueOf(100);

        Conta contaDebitoMock = criaContaEntidade();
        Conta contaCreditoMock = criaContaEntidade();
        contaCreditoMock.setNumeroAgencia(1);
        contaCreditoMock.setNumeroConta(102031L);

        BigDecimal saldoContaDebitoAntes = contaDebitoMock.getSaldo();
        BigDecimal saldoContaCreditoAntes = contaCreditoMock.getSaldo();

        Lancamento lancamentoDebitoMock = criaLancamentoEntidade(tipoLancamento, Natureza.DEBITO, valor);
        Lancamento lancamentoCreditoMock = criaLancamentoEntidade(tipoLancamento, Natureza.CREDITO, valor);

        TransferenciaRequestDTO transferenciaRequest = criaTransferenciaRequestDTO(contaCreditoMock.getNumeroAgencia(), contaCreditoMock.getNumeroConta(), valor);

        when(contaRepository.findById(eq(contaDebitoMock.getId()))).thenReturn(Optional.of(contaDebitoMock));
        when(contaRepository.findByNumeroAgenciaAndNumeroConta(eq(contaCreditoMock.getNumeroAgencia()), eq(contaCreditoMock.getNumeroConta()))).thenReturn(Optional.of(contaCreditoMock));
        when(lancamentoService.efetuaLancamento(any(LancamentoRequestDTO.class), eq(contaDebitoMock), eq(Natureza.DEBITO), eq(tipoLancamento))).thenReturn(lancamentoDebitoMock);
        when(lancamentoService.efetuaLancamento(any(LancamentoRequestDTO.class), eq(contaCreditoMock), eq(Natureza.CREDITO), eq(tipoLancamento))).thenReturn(lancamentoCreditoMock);
        when(lancamentoService.efetuaTransferencia(eq(lancamentoDebitoMock), eq(lancamentoCreditoMock))).thenReturn(criaComprovanteResponseDTO());

        ComprovanteResponseDTO comprovanteResponse = contaService.efetuaTransferencia(contaDebitoMock.getId(), transferenciaRequest);

        verify(contaRepository).findById(eq(contaDebitoMock.getId()));
        verify(contaRepository).findByNumeroAgenciaAndNumeroConta(eq(contaCreditoMock.getNumeroAgencia()), eq(contaCreditoMock.getNumeroConta()));
        verify(lancamentoService).efetuaLancamento(any(LancamentoRequestDTO.class), eq(contaDebitoMock), eq(Natureza.DEBITO), eq(TipoLancamento.TRANSFERENCIA));
        verify(lancamentoService).efetuaLancamento(any(LancamentoRequestDTO.class), eq(contaCreditoMock), eq(Natureza.CREDITO), eq(TipoLancamento.TRANSFERENCIA));
        verify(contaRepository).save(eq(contaDebitoMock));
        verify(contaRepository).save(eq(contaCreditoMock));
        verify(lancamentoService).efetuaTransferencia(eq(lancamentoDebitoMock), eq(lancamentoCreditoMock));
        verifyNoMoreInteractions(contaRepository);
        verifyNoMoreInteractions(lancamentoService);

        assertEquals(saldoContaDebitoAntes.subtract(valor), contaDebitoMock.getSaldo());
        assertEquals(saldoContaCreditoAntes.add(valor), contaCreditoMock.getSaldo());
        assertNotNull(comprovanteResponse);
    }

    @Test
    public void efetuaTransferenciaContaDebitoInexistente() {
        BigDecimal valor = BigDecimal.valueOf(100);

        Conta contaDebitoMock = criaContaEntidade();
        Conta contaCreditoMock = criaContaEntidade();
        contaCreditoMock.setNumeroAgencia(1);
        contaCreditoMock.setNumeroConta(102031L);

        TransferenciaRequestDTO transferenciaRequest = criaTransferenciaRequestDTO(contaCreditoMock.getNumeroAgencia(), contaCreditoMock.getNumeroConta(), valor);

        when(contaRepository.findById(eq(contaDebitoMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.efetuaTransferencia(contaDebitoMock.getId(), transferenciaRequest));

        verify(contaRepository).findById(eq(contaDebitoMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-3", serviceException.getCodigoErro());
    }

    @Test
    public void efetuaTransferenciaContaCreditoInexistente() {
        BigDecimal valor = BigDecimal.valueOf(100);

        Conta contaDebitoMock = criaContaEntidade();
        Conta contaCreditoMock = criaContaEntidade();
        contaCreditoMock.setNumeroAgencia(1);
        contaCreditoMock.setNumeroConta(102031L);

        TransferenciaRequestDTO transferenciaRequest = criaTransferenciaRequestDTO(contaCreditoMock.getNumeroAgencia(), contaCreditoMock.getNumeroConta(), valor);

        when(contaRepository.findById(eq(contaDebitoMock.getId()))).thenReturn(Optional.of(contaDebitoMock));
        when(contaRepository.findByNumeroAgenciaAndNumeroConta(eq(contaCreditoMock.getNumeroAgencia()), eq(contaCreditoMock.getNumeroConta()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.efetuaTransferencia(contaDebitoMock.getId(), transferenciaRequest));

        verify(contaRepository).findById(eq(contaDebitoMock.getId()));
        verify(contaRepository).findByNumeroAgenciaAndNumeroConta(eq(contaCreditoMock.getNumeroAgencia()), eq(contaCreditoMock.getNumeroConta()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-5", serviceException.getCodigoErro());
    }

    @Test
    public void consultaExtratoCompletoComSucesso() {
        Conta contaMock = criaContaEntidade();
        List<ComprovanteResponseDTO> comprovantes = Arrays.asList(criaComprovanteResponseDTO(), criaComprovanteResponseDTO());

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));
        when(lancamentoService.consultaExtratoCompleto(eq(contaMock))).thenReturn(comprovantes);

        ExtratoResponseDTO extratoResponse = contaService.consultaExtratoCompleto(contaMock.getId());

        verify(contaRepository).findById(eq(contaMock.getId()));
        verify(lancamentoService).consultaExtratoCompleto(eq(contaMock));
        verifyNoMoreInteractions(contaRepository);
        verifyNoMoreInteractions(lancamentoService);

        assertEquals(comprovantes.size(), extratoResponse.getLancamentos().size());
    }

    @Test
    public void consultaExtratoCompletoContaInexistente() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.consultaExtratoCompleto(contaMock.getId()));

        verify(contaRepository).findById(eq(contaMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-3", serviceException.getCodigoErro());
    }

    @Test
    public void consultaExtratoPeriodoComSucesso() {
        Conta contaMock = criaContaEntidade();
        List<ComprovanteResponseDTO> comprovantes = Arrays.asList(criaComprovanteResponseDTO(), criaComprovanteResponseDTO());
        LocalDate dataInicial = LocalDate.now();
        LocalDate dataFinal = LocalDate.now();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));
        when(lancamentoService.consultaExtratoPorPeriodo(eq(contaMock), eq(dataInicial), eq(dataFinal))).thenReturn(comprovantes);

        ExtratoResponseDTO extratoResponse = contaService.consultaExtratoPorPeriodo(contaMock.getId(), dataInicial, dataFinal);

        verify(contaRepository).findById(eq(contaMock.getId()));
        verify(lancamentoService).consultaExtratoPorPeriodo(eq(contaMock), eq(dataInicial), eq(dataFinal));
        verifyNoMoreInteractions(contaRepository);
        verifyNoMoreInteractions(lancamentoService);

        assertEquals(comprovantes.size(), extratoResponse.getLancamentos().size());
    }

    @Test
    public void consultaExtratoPeriodoContaInexistente() {
        Conta contaMock = criaContaEntidade();
        LocalDate dataInicial = LocalDate.now();
        LocalDate dataFinal = LocalDate.now();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.consultaExtratoPorPeriodo(contaMock.getId(), dataInicial, dataFinal));

        verify(contaRepository).findById(eq(contaMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-3", serviceException.getCodigoErro());
    }

    @Test
    public void estornaLancamentoComSucesso() {
        Long idConta = 1L;
        Long idLancamento = 1L;

        when(lancamentoService.estornaLancamento(eq(idConta), eq(idLancamento))).thenReturn(criaComprovanteResponseDTO());

        ComprovanteResponseDTO comprovanteResponse = contaService.estornaLancamento(idConta, idLancamento);

        verify(lancamentoService).estornaLancamento(eq(idConta), eq(idLancamento));
        verifyNoMoreInteractions(lancamentoService);
        verifyNoInteractions(contaRepository);

        assertNotNull(comprovanteResponse);
    }

    @Test
    public void consultaComprovanteLancamentoComSucesso() {
        Long idConta = 1L;
        Long idLancamento = 1L;

        when(lancamentoService.consultaComprovanteLancamento(eq(idConta), eq(idLancamento))).thenReturn(criaComprovanteResponseDTO());

        ComprovanteResponseDTO comprovanteResponse = contaService.consultaComprovanteLancamento(idConta, idLancamento);

        verify(lancamentoService).consultaComprovanteLancamento(eq(idConta), eq(idLancamento));
        verifyNoMoreInteractions(lancamentoService);
        verifyNoInteractions(contaRepository);

        assertNotNull(comprovanteResponse);
    }

    @Test
    public void removeLancamentoEstornoComSucesso() {
        Long idConta = 1L;
        Long idLancamento = 1L;

        contaService.removeLancamentoEstorno(idConta, idLancamento);

        verify(lancamentoService).removeLancamentoEstorno(eq(idConta), eq(idLancamento));
        verifyNoMoreInteractions(lancamentoService);
        verifyNoInteractions(contaRepository);
    }

    @Test
    public void consultaTodasAsContasComSucesso() {
        List<Conta> contasMock = Arrays.asList(criaContaEntidade(), criaContaEntidade());

        when(contaRepository.findAll()).thenReturn(contasMock);

        List<ContaResponseDTO> contasResponse = contaService.consultaTodas();

        verify(contaRepository).findAll();
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals(contasMock.size(), contasResponse.size());
    }

    @Test
    public void consultaContaPorIdClienteComSucesso() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findByCliente_Id(eq(contaMock.getCliente().getId()))).thenReturn(Optional.of(contaMock));

        ContaResponseDTO contaResponse = contaService.consultaContaPorIdCliente(contaMock.getCliente().getId());

        verify(contaRepository).findByCliente_Id(eq(contaMock.getCliente().getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertNotNull(contaResponse);
    }

    @Test
    public void consultaContaPorIdClienteInexistente() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findByCliente_Id(eq(contaMock.getCliente().getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.consultaContaPorIdCliente(contaMock.getCliente().getId()));

        verify(contaRepository).findByCliente_Id(eq(contaMock.getCliente().getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-12", serviceException.getCodigoErro());
    }

    @Test
    public void bloqueiaContaComSucesso() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));

        contaService.bloqueiaConta(contaMock.getId());

        verify(contaRepository).findById(eq(contaMock.getId()));
        verify(contaRepository).save(eq(contaMock));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals(SituacaoConta.BLOQUEADA.getCodigo(), contaMock.getSituacao());
    }

    @Test
    public void bloqueiaContaInexistente() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.bloqueiaConta(contaMock.getId()));

        verify(contaRepository).findById(eq(contaMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-3", serviceException.getCodigoErro());
    }

    @Test
    public void bloqueiaContaJaBloqueada() {
        Conta contaMock = criaContaEntidade();
        contaMock.setSituacao(SituacaoConta.BLOQUEADA.getCodigo());

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.bloqueiaConta(contaMock.getId()));

        verify(contaRepository).findById(eq(contaMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-13", serviceException.getCodigoErro());
    }

    @Test
    public void desbloqueiaContaComSucesso() {
        Conta contaMock = criaContaEntidade();
        contaMock.setSituacao(SituacaoConta.BLOQUEADA.getCodigo());

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));

        contaService.desbloqueiaConta(contaMock.getId());

        verify(contaRepository).findById(eq(contaMock.getId()));
        verify(contaRepository).save(eq(contaMock));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals(SituacaoConta.ABERTA.getCodigo(), contaMock.getSituacao());
    }

    @Test
    public void desbloqueiaContaInexistente() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.desbloqueiaConta(contaMock.getId()));

        verify(contaRepository).findById(eq(contaMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-3", serviceException.getCodigoErro());
    }

    @Test
    public void desbloqueiaContaJaAberta() {
        Conta contaMock = criaContaEntidade();

        when(contaRepository.findById(eq(contaMock.getId()))).thenReturn(Optional.of(contaMock));

        ServiceException serviceException = assertThrows(ServiceException.class, () -> contaService.desbloqueiaConta(contaMock.getId()));

        verify(contaRepository).findById(eq(contaMock.getId()));
        verifyNoMoreInteractions(contaRepository);
        verifyNoInteractions(lancamentoService);

        assertEquals("DB-14", serviceException.getCodigoErro());
    }

}
