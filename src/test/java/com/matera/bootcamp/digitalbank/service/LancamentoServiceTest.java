package com.matera.bootcamp.digitalbank.service;

import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaContaEntidade;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaLancamentoEntidade;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaLancamentoRequestDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaTransferenciaEntidade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.matera.bootcamp.digitalbank.dto.request.LancamentoRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ComprovanteResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Conta;
import com.matera.bootcamp.digitalbank.entity.Estorno;
import com.matera.bootcamp.digitalbank.entity.Lancamento;
import com.matera.bootcamp.digitalbank.entity.Transferencia;
import com.matera.bootcamp.digitalbank.enumerator.Natureza;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;
import com.matera.bootcamp.digitalbank.enumerator.TipoLancamento;
import com.matera.bootcamp.digitalbank.exception.ServiceException;
import com.matera.bootcamp.digitalbank.repository.EstornoRepository;
import com.matera.bootcamp.digitalbank.repository.LancamentoRepository;
import com.matera.bootcamp.digitalbank.repository.TransferenciaRepository;

@ExtendWith(MockitoExtension.class)
public class LancamentoServiceTest {

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private EstornoRepository estornoRepository;

    @InjectMocks
    private LancamentoService lancamentoService;

    @Test
    public void efetuaLancamentoCreditoComSucesso() {
        BigDecimal valor = BigDecimal.valueOf(10000);
        LancamentoRequestDTO lancamentoRequest = criaLancamentoRequestDTO(valor);
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.DEPOSITO, Natureza.CREDITO, valor);
        Conta conta = criaContaEntidade();

        when(lancamentoRepository.save(any(Lancamento.class))).thenReturn(lancamentoMock);

        Lancamento lancamento = lancamentoService.efetuaLancamento(lancamentoRequest, conta, Natureza.CREDITO, TipoLancamento.DEPOSITO);

        verify(lancamentoRepository).save(any(Lancamento.class));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals(lancamentoMock, lancamento);
    }

    @Test
    public void efetuaLancamentoCreditoContaBloqueada() {
        LancamentoRequestDTO lancamentoRequest = criaLancamentoRequestDTO(BigDecimal.valueOf(10000));
        Conta conta = criaContaEntidade();
        conta.setSituacao(SituacaoConta.BLOQUEADA.getCodigo());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.efetuaLancamento(lancamentoRequest, conta, Natureza.CREDITO, TipoLancamento.DEPOSITO));

        verifyNoInteractions(lancamentoRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals("DB-15", serviceException.getCodigoErro());
    }

    @Test
    public void efetuaLancamentoDebitoComSucesso() {
        BigDecimal valor = BigDecimal.valueOf(5000);
        LancamentoRequestDTO lancamentoRequest = criaLancamentoRequestDTO(valor);
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, valor);
        Conta conta = criaContaEntidade();

        when(lancamentoRepository.save(any(Lancamento.class))).thenReturn(lancamentoMock);

        Lancamento lancamento = lancamentoService.efetuaLancamento(lancamentoRequest, conta, Natureza.DEBITO, TipoLancamento.SAQUE);

        verify(lancamentoRepository).save(any(Lancamento.class));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals(lancamentoMock, lancamento);
    }

    @Test
    public void efetuaLancamentoDebitoSemSaldo() {
        LancamentoRequestDTO lancamentoRequest = criaLancamentoRequestDTO(BigDecimal.valueOf(6000));
        Conta conta = criaContaEntidade();

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.efetuaLancamento(lancamentoRequest, conta, Natureza.DEBITO, TipoLancamento.SAQUE));

        verifyNoInteractions(lancamentoRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals("DB-6", serviceException.getCodigoErro());
    }

    @Test
    public void efetuaTransferenciaComSucesso() {
        BigDecimal valor = BigDecimal.valueOf(100);
        Lancamento lancamentoDebito = criaLancamentoEntidade(TipoLancamento.TRANSFERENCIA, Natureza.DEBITO, valor);
        Lancamento lancamentoCredito = criaLancamentoEntidade(TipoLancamento.TRANSFERENCIA, Natureza.CREDITO, valor);

        ComprovanteResponseDTO comprovanteResponse = lancamentoService.efetuaTransferencia(lancamentoDebito, lancamentoCredito);

        verify(transferenciaRepository).save(any(Transferencia.class));
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoInteractions(lancamentoRepository);
        verifyNoInteractions(estornoRepository);

        assertNotNull(comprovanteResponse);
    }

    @Test
    public void consultaExtratoCompletoComSucesso() {
        Conta conta = criaContaEntidade();
        List<Lancamento> lancamentosMock = Arrays.asList(criaLancamentoEntidade(TipoLancamento.DEPOSITO, Natureza.CREDITO, BigDecimal.valueOf(100)),
                                                         criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(200)));

        when(lancamentoRepository.findByConta_IdOrderByIdDesc(eq(conta.getId()))).thenReturn(lancamentosMock);

        List<ComprovanteResponseDTO> lancamentosResponse = lancamentoService.consultaExtratoCompleto(conta);

        verify(lancamentoRepository).findByConta_IdOrderByIdDesc(eq(conta.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals(lancamentosMock.size(), lancamentosResponse.size());
    }

    @Test
    public void consultaExtratoPorPeriodoComSucesso() {
        LocalDate dataInicial = LocalDate.now();
        LocalDate dataFinal = LocalDate.now();
        Conta conta = criaContaEntidade();
        List<Lancamento> lancamentosMock = Arrays.asList(criaLancamentoEntidade(TipoLancamento.DEPOSITO, Natureza.CREDITO, BigDecimal.valueOf(100)),
                                                         criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(200)));

        when(lancamentoRepository.consultaLancamentosPorPeriodo(eq(conta.getId()), eq(dataInicial), eq(dataFinal))).thenReturn(lancamentosMock);

        List<ComprovanteResponseDTO> lancamentosResponse = lancamentoService.consultaExtratoPorPeriodo(conta, dataInicial, dataFinal);

        verify(lancamentoRepository).consultaLancamentosPorPeriodo(eq(conta.getId()), eq(dataInicial), eq(dataFinal));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals(lancamentosMock.size(), lancamentosResponse.size());
    }

    @Test
    public void estornaLancamentoDebitoComSucesso() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(100));

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.of(lancamentoMock));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());

        ComprovanteResponseDTO comprovanteResponse = lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId());

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoMock.getId()));
        verify(lancamentoRepository, times(2)).save(any(Lancamento.class));
        verify(estornoRepository).save(any(Estorno.class));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertNotNull(comprovanteResponse);
        assertEquals(TipoLancamento.ESTORNO.getCodigo(), comprovanteResponse.getTipoLancamento());
        assertEquals(Natureza.CREDITO.getCodigo(), comprovanteResponse.getNatureza());
    }

    @Test
    public void estornaLancamentoCreditoComSucesso() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.CREDITO, BigDecimal.valueOf(100));

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.of(lancamentoMock));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());

        ComprovanteResponseDTO comprovanteResponse = lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId());

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoMock.getId()));
        verify(lancamentoRepository, times(2)).save(any(Lancamento.class));
        verify(estornoRepository).save(any(Estorno.class));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertNotNull(comprovanteResponse);
        assertEquals(TipoLancamento.ESTORNO.getCodigo(), comprovanteResponse.getTipoLancamento());
        assertEquals(Natureza.DEBITO.getCodigo(), comprovanteResponse.getNatureza());
    }

    @Test
    public void estornaLancamentoTransferenciaSucesso() {
        Transferencia transferenciaMock = criaTransferenciaEntidade(BigDecimal.valueOf(100));
        Lancamento lancamentoCredito = transferenciaMock.getLancamentoCredito();

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoCredito.getId()), eq(lancamentoCredito.getConta().getId()))).thenReturn(Optional.of(lancamentoCredito));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoCredito.getId()))).thenReturn(Optional.of(transferenciaMock));
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoCredito.getId()))).thenReturn(Optional.empty());

        ComprovanteResponseDTO comprovanteResponse = lancamentoService.estornaLancamento(lancamentoCredito.getConta().getId(), lancamentoCredito.getId());

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoCredito.getId()), eq(lancamentoCredito.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoCredito.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoCredito.getId()));
        verify(lancamentoRepository, times(4)).save(any(Lancamento.class));
        verify(estornoRepository, times(2)).save(any(Estorno.class));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertNotNull(comprovanteResponse);
    }

    @Test
    public void estornaLancamentoDebitoInexistente() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(100));

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.empty());
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());
        
        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId()));

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals("DB-7", serviceException.getCodigoErro());
    }

    @Test
    public void estornaLancamentoEstorno() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(100));
        lancamentoMock.setTipoLancamento(TipoLancamento.ESTORNO.getCodigo());

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.of(lancamentoMock));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId()));

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoInteractions(estornoRepository);

        assertEquals("DB-8", serviceException.getCodigoErro());
    }

    @Test
    public void estornaLancamentoJaEstornado() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(100));

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.of(lancamentoMock));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoMock.getId()))).thenReturn(Optional.of(new Estorno()));

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId()));

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoMock.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertEquals("DB-9", serviceException.getCodigoErro());
    }

    @Test
    public void estornaLancamentoTransferenciaPeloDebito() {
        Transferencia transferenciaMock = criaTransferenciaEntidade(BigDecimal.valueOf(100));
        Lancamento lancamentoDebito = transferenciaMock.getLancamentoDebito();

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoDebito.getId()), eq(lancamentoDebito.getConta().getId()))).thenReturn(Optional.of(lancamentoDebito));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoDebito.getId()))).thenReturn(Optional.of(transferenciaMock));
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoDebito.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.estornaLancamento(lancamentoDebito.getConta().getId(), lancamentoDebito.getId()));

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoDebito.getId()), eq(lancamentoDebito.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoDebito.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoDebito.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertEquals("DB-10", serviceException.getCodigoErro());
    }

    @Test
    public void estornaLancamentoContaBloqueada() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.DEBITO, BigDecimal.valueOf(100));
        lancamentoMock.getConta().setSituacao(SituacaoConta.BLOQUEADA.getCodigo());

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.of(lancamentoMock));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId()));

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoMock.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertEquals("DB-15", serviceException.getCodigoErro());
    }


    @Test
    public void estornaLancamentoCreditoContaSemSaldo() {
        Lancamento lancamentoMock = criaLancamentoEntidade(TipoLancamento.SAQUE, Natureza.CREDITO, BigDecimal.valueOf(100));
        lancamentoMock.getConta().setSaldo(BigDecimal.ZERO);

        when(lancamentoRepository.findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()))).thenReturn(Optional.of(lancamentoMock));
        when(transferenciaRepository.consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());
        when(estornoRepository.findByLancamentoOriginal_Id(eq(lancamentoMock.getId()))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> lancamentoService.estornaLancamento(lancamentoMock.getConta().getId(), lancamentoMock.getId()));

        verify(lancamentoRepository).findByIdAndConta_Id(eq(lancamentoMock.getId()), eq(lancamentoMock.getConta().getId()));
        verify(transferenciaRepository).consultaTransferenciaPorIdLancamento(eq(lancamentoMock.getId()));
        verify(estornoRepository).findByLancamentoOriginal_Id(eq(lancamentoMock.getId()));
        verifyNoMoreInteractions(lancamentoRepository);
        verifyNoMoreInteractions(transferenciaRepository);
        verifyNoMoreInteractions(estornoRepository);

        assertEquals("DB-11", serviceException.getCodigoErro());
    }

}
