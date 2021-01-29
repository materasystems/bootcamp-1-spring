package com.matera.bootcamp.digitalbank.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.matera.bootcamp.digitalbank.utils.DigitalBankUtils;

@Service
public class ContaService {

	@Value("${agencia.numeroMaximo:3}")
	private Integer numeroMaximoAgencia;
	
	private ContaRepository contaRepository;
	private LancamentoService lancamentoService;

	public ContaService(ContaRepository contaRepository, LancamentoService lancamentoService) {
		this.contaRepository = contaRepository;
		this.lancamentoService = lancamentoService;
	}

	@Transactional
	public ContaResponseDTO cadastra(Cliente cliente) {
		validaCadastro(cliente);

		Conta conta = Conta.builder().numeroAgencia(new Random().nextInt(numeroMaximoAgencia) + 1)
									 .numeroConta(cliente.getTelefone())
									 .saldo(BigDecimal.ZERO)
									 .situacao(SituacaoConta.ABERTA.getCodigo())
									 .cliente(cliente)
									 .build();
		
		return entidadeParaResponseDTO(contaRepository.save(conta));
	}
	
    public List<ContaResponseDTO> consultaTodas() {
	    List<Conta> contas = contaRepository.findAll();
	    List<ContaResponseDTO> contasResponseDTO = new ArrayList<>();

	    contas.forEach(conta -> contasResponseDTO.add(entidadeParaResponseDTO(conta)));

        return contasResponseDTO;
    }
    
    public ContaResponseDTO consultaContaPorIdCliente(Long idCliente) {
	    Conta conta = contaRepository.findByCliente_Id(idCliente)
	    							 .orElseThrow(() -> new ServiceException("DB-12", idCliente));

	    return entidadeParaResponseDTO(conta);
	}
    
	@Transactional
	public void bloqueiaConta(Long id) {
	    Conta conta = buscaPorId(id);

	    validaBloqueio(conta);

	    conta.setSituacao(SituacaoConta.BLOQUEADA.getCodigo());
	    contaRepository.save(conta);
	}

	@Transactional
	public void desbloqueiaConta(Long id) {
        Conta conta = buscaPorId(id);

        validaDesbloqueio(conta);

        conta.setSituacao(SituacaoConta.ABERTA.getCodigo());
        contaRepository.save(conta);
    }
	
    @Transactional
	public ComprovanteResponseDTO efetuaLancamento(Long id, LancamentoRequestDTO lancamentoRequestDTO, TipoLancamento tipoLancamento) {
		Conta conta = buscaPorId(id);

		Lancamento lancamento = insereLancamento(lancamentoRequestDTO, conta, defineNaturezaPorTipoLancamento(tipoLancamento), tipoLancamento);

		return lancamentoService.entidadeParaComprovanteResponseDTO(lancamento);
	}
    
    @Transactional
	public ComprovanteResponseDTO efetuaTransferencia(Long id, TransferenciaRequestDTO transferenciaRequestDTO) {
		Conta contaDebito = buscaPorId(id);

		Conta contaCredito = contaRepository.findByNumeroAgenciaAndNumeroConta(transferenciaRequestDTO.getNumeroAgencia(), transferenciaRequestDTO.getNumeroConta())
		                                    .orElseThrow(() -> new ServiceException("DB-5", transferenciaRequestDTO.getNumeroAgencia(), transferenciaRequestDTO.getNumeroConta()));

		Lancamento lancamentoDebito = insereLancamento(new LancamentoRequestDTO(transferenciaRequestDTO.getValor(), transferenciaRequestDTO.getDescricao()), contaDebito, Natureza.DEBITO, TipoLancamento.TRANSFERENCIA);
		Lancamento lancamentoCredito = insereLancamento(new LancamentoRequestDTO(transferenciaRequestDTO.getValor(), transferenciaRequestDTO.getDescricao()), contaCredito, Natureza.CREDITO, TipoLancamento.TRANSFERENCIA);

		return lancamentoService.efetuaTransferencia(lancamentoDebito, lancamentoCredito);
	}

	public ExtratoResponseDTO consultaExtratoCompleto(Long id) {
		Conta conta = buscaPorId(id);

		List<ComprovanteResponseDTO> comprovantesResponseDTO = lancamentoService.consultaExtratoCompleto(conta);

		ExtratoResponseDTO extratoResponseDTO = new ExtratoResponseDTO();
		extratoResponseDTO.setConta(entidadeParaResponseDTO(conta));
		extratoResponseDTO.setLancamentos(comprovantesResponseDTO);

		return extratoResponseDTO;
	}

	public ExtratoResponseDTO consultaExtratoPorPeriodo(Long id, LocalDate dataInicial, LocalDate dataFinal) {
		Conta conta = buscaPorId(id);

		List<ComprovanteResponseDTO> comprovantesResponseDTO = lancamentoService.consultaExtratoPorPeriodo(conta, dataInicial, dataFinal);

		ExtratoResponseDTO extratoResponseDTO = new ExtratoResponseDTO();
		extratoResponseDTO.setConta(entidadeParaResponseDTO(conta));
		extratoResponseDTO.setLancamentos(comprovantesResponseDTO);

		return extratoResponseDTO;
	}

	@Transactional
	public ComprovanteResponseDTO estornaLancamento(Long idConta, Long idLancamento) {
		return lancamentoService.estornaLancamento(idConta, idLancamento);
	}

	public ComprovanteResponseDTO consultaComprovanteLancamento(Long idConta, Long idLancamento) {
		return lancamentoService.consultaComprovanteLancamento(idConta, idLancamento);
	}
	
	@Transactional
	public void removeLancamentoEstorno(Long idConta, Long idLancamento) {
	    lancamentoService.removeLancamentoEstorno(idConta, idLancamento);
	}
	
    private Lancamento insereLancamento(LancamentoRequestDTO lancamentoRequestDTO, Conta conta, Natureza natureza, TipoLancamento tipoLancamento) {
	    Lancamento lancamento = lancamentoService.efetuaLancamento(lancamentoRequestDTO, conta, natureza, tipoLancamento);

		atualizaSaldo(conta, lancamento.getValor(), natureza);

        return lancamento;
	}
    
    private void atualizaSaldo(Conta conta, BigDecimal valorLancamento, Natureza natureza) {
        BigDecimal saldo = DigitalBankUtils.calculaSaldo(natureza, valorLancamento, conta.getSaldo());

		conta.setSaldo(saldo);
		contaRepository.save(conta);
    }
    
    private Natureza defineNaturezaPorTipoLancamento(TipoLancamento tipoLancamento) {
        return TipoLancamento.DEPOSITO.equals(tipoLancamento) ? Natureza.CREDITO : Natureza.DEBITO;
    }
	
	private Conta buscaPorId(Long id) {
		return contaRepository.findById(id)
							  .orElseThrow(() -> new ServiceException("DB-3", id));
	}

	private void validaCadastro(Cliente cliente) {
		if (contaRepository.findByNumeroConta(cliente.getTelefone()).isPresent()) {
			throw new ServiceException("DB-4", cliente.getTelefone());
		}
	}
	
    private void validaBloqueio(Conta conta) {
        if (SituacaoConta.BLOQUEADA.getCodigo().equals(conta.getSituacao())) {
            throw new ServiceException("DB-13", conta.getId());
        }
    }
    
    private void validaDesbloqueio(Conta conta) {
        if (SituacaoConta.ABERTA.getCodigo().equals(conta.getSituacao())) {
            throw new ServiceException("DB-14", conta.getId());
        }
    }
    
    private ContaResponseDTO entidadeParaResponseDTO(Conta conta) {
        return ContaResponseDTO.builder().idCliente(conta.getCliente().getId())
                                         .idConta(conta.getId())
                                         .numeroAgencia(conta.getNumeroAgencia())
                                         .numeroConta(conta.getNumeroConta())
                                         .saldo(conta.getSaldo())
                                         .situacao(conta.getSituacao())
                                         .build();
    }

}
