package com.matera.bootcamp.digitalbank.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.matera.bootcamp.digitalbank.utils.DigitalBankUtils;

@Service
public class LancamentoService {
	
	private static final String COMPLEMENTO_ESTORNO = " - Estornado";

	private final LancamentoRepository lancamentoRepository;
	private final TransferenciaRepository transferenciaRepository;
	private final EstornoRepository estornoRepository;

	public LancamentoService(LancamentoRepository lancamentoRepository, TransferenciaRepository transferenciaRepository, EstornoRepository estornoRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.estornoRepository = estornoRepository;
    }

	@Transactional
    public Lancamento efetuaLancamento(LancamentoRequestDTO lancamentoRequestDTO, Conta conta, Natureza natureza, TipoLancamento tipoLancamento) {
		Lancamento lancamento = Lancamento.builder().dataHora(LocalDateTime.now())
                                            		.codigoAutenticacao(geraAutenticacao())
                                            		.valor(lancamentoRequestDTO.getValor())
                                            		.natureza(natureza.getCodigo())
                                            		.tipoLancamento(tipoLancamento.getCodigo())
                                            		.descricao(lancamentoRequestDTO.getDescricao())
                                            		.conta(conta)
                                            		.build();

		validaLancamento(lancamento);

		return lancamentoRepository.save(lancamento);
	}

	@Transactional
    public ComprovanteResponseDTO efetuaTransferencia(Lancamento lancamentoDebito, Lancamento lancamentoCredito) {
		Transferencia transferencia = new Transferencia(lancamentoDebito, lancamentoCredito);

		transferenciaRepository.save(transferencia);

		return entidadeParaComprovanteResponseDTO(lancamentoDebito);
	}

	public List<ComprovanteResponseDTO> consultaExtratoCompleto(Conta conta) {
		List<Lancamento> lancamentos = lancamentoRepository.findByConta_IdOrderByIdDesc(conta.getId());

		List<ComprovanteResponseDTO> comprovantesResponseDTO = new ArrayList<>();
		lancamentos.forEach(l -> comprovantesResponseDTO.add(entidadeParaComprovanteResponseDTO(l)));

		return comprovantesResponseDTO;
	}

	public List<ComprovanteResponseDTO> consultaExtratoPorPeriodo(Conta conta, LocalDate dataInicial, LocalDate dataFinal) {
		List<Lancamento> lancamentos = lancamentoRepository.consultaLancamentosPorPeriodo(conta.getId(), dataInicial, dataFinal);

		List<ComprovanteResponseDTO> comprovantesResponseDTO = new ArrayList<>();
		lancamentos.forEach(l -> comprovantesResponseDTO.add(entidadeParaComprovanteResponseDTO(l)));

		return comprovantesResponseDTO;
	}

	@Transactional
	public ComprovanteResponseDTO estornaLancamento(Long idConta, Long idLancamento) {
		Lancamento lancamento = lancamentoRepository.findByIdAndConta_Id(idLancamento, idConta).orElse(null);
		Transferencia transferencia = transferenciaRepository.consultaTransferenciaPorIdLancamento(idLancamento).orElse(null);

		validaEstorno(lancamento, transferencia, idConta, idLancamento);

		if (transferencia != null) {
			return trataEstornoTransferencia(transferencia);
		} else {
			return trataEstornoLancamento(lancamento);
		}
	}

	public ComprovanteResponseDTO consultaComprovanteLancamento(Long idConta, Long idLancamento) {
		Lancamento lancamento = buscaLancamentoConta(idConta, idLancamento);

		return entidadeParaComprovanteResponseDTO(lancamento);
	}

	@Transactional
	public void removeLancamentoEstorno(Long idConta, Long idLancamento) {
	    Lancamento lancamentoEstorno = buscaLancamentoConta(idConta, idLancamento);
	    Estorno estorno = estornoRepository.findByLancamentoEstorno_Id(idLancamento)
	                                       .orElseThrow(() -> new ServiceException("Somente lançamentos de estorno podem ser removidos."));
	    Lancamento lancamentoOriginal = estorno.getLancamentoOriginal();

	    lancamentoOriginal.getConta().setSaldo(DigitalBankUtils.calculaSaldo(Natureza.buscaPorCodigo(lancamentoOriginal.getNatureza()),
	                                                                         lancamentoOriginal.getValor(),
	                                                                         lancamentoOriginal.getConta().getSaldo()));
	    lancamentoOriginal.setDescricao(lancamentoOriginal.getDescricao().replace(COMPLEMENTO_ESTORNO, ""));

	    lancamentoRepository.save(lancamentoOriginal);
	    estornoRepository.delete(estorno);
	    lancamentoRepository.delete(lancamentoEstorno);
	}

	public ComprovanteResponseDTO entidadeParaComprovanteResponseDTO(Lancamento lancamento) {
		return ComprovanteResponseDTO.builder().idLancamento(lancamento.getId())
                                        	   .codigoAutenticacao(lancamento.getCodigoAutenticacao())
                                        	   .dataHora(lancamento.getDataHora())
                                        	   .valor(lancamento.getValor())
                                        	   .natureza(lancamento.getNatureza())
                                        	   .tipoLancamento(lancamento.getTipoLancamento())
                                        	   .descricao(lancamento.getDescricao())
                                        	   .build();
	}
	
	private Lancamento buscaLancamentoConta(Long idConta, Long idLancamento) {
		return lancamentoRepository.findByIdAndConta_Id(idLancamento, idConta)
													.orElseThrow(() -> new ServiceException("O lançamento de ID " + idLancamento + " não existe para a conta de ID " + idConta + "."));
	}

	private void validaLancamento(Lancamento lancamento) {
	    if (SituacaoConta.BLOQUEADA.getCodigo().equals(lancamento.getConta().getSituacao())) {
            throw new ServiceException("Conta de ID " + lancamento.getConta().getId() + " está na situação Bloqueada. Novos lançamentos não são permitidos.");
        }

	    if (Natureza.DEBITO.getCodigo().equals(lancamento.getNatureza()) && lancamento.getConta().getSaldo().compareTo(lancamento.getValor()) < 0) {
            throw new ServiceException("Saldo indisponível para efetuar o lançamento.");
        }
    }

	private void validaEstorno(Lancamento lancamento, Transferencia transferencia, Long idConta, Long idLancamento) {
		if (lancamento == null) {
			throw new ServiceException("O lançamento de ID " + idLancamento + " não existe para a conta de ID " + idConta + ".");
		}

		if (TipoLancamento.ESTORNO.getCodigo().equals(lancamento.getTipoLancamento())) {
			throw new ServiceException("Tipo de lançamento " + lancamento.getTipoLancamento() + " não permite estornos.");
		}

		if (estornoRepository.findByLancamentoOriginal_Id(lancamento.getId()).isPresent()) {
			throw new ServiceException("O lançamento informado já está estornado.");
		}

		if (TipoLancamento.TRANSFERENCIA.getCodigo().equals(lancamento.getTipoLancamento()) && !lancamento.getId().equals(transferencia.getLancamentoCredito().getId())) {
			throw new ServiceException("Estorno de transferência só pode ser solicitado pela conta creditada.");
		}

		if (SituacaoConta.BLOQUEADA.getCodigo().equals(lancamento.getConta().getSituacao())) {
            throw new ServiceException("Conta de ID " + lancamento.getConta().getId() + " está na situação Bloqueada. Novos lançamentos não são permitidos.");
        }

		if (Natureza.CREDITO.getCodigo().equals(lancamento.getNatureza()) && lancamento.getConta().getSaldo().compareTo(lancamento.getValor()) < 0) {
            throw new ServiceException("Saldo indisponível para estornar o lançamento de crédito.");
        }
	}

	private ComprovanteResponseDTO trataEstornoTransferencia(Transferencia transferencia) {
		trataEstornoLancamento(transferencia.getLancamentoDebito());
		return trataEstornoLancamento(transferencia.getLancamentoCredito());
	}

	private ComprovanteResponseDTO trataEstornoLancamento(Lancamento lancamento) {
		Conta conta = lancamento.getConta();
		Natureza natureza = defineNaturezaEstorno(lancamento);
		conta.setSaldo(DigitalBankUtils.calculaSaldo(natureza, lancamento.getValor(), conta.getSaldo()));

		Lancamento lancamentoEstorno = Lancamento.builder().codigoAutenticacao(geraAutenticacao())
														   .conta(conta)
														   .dataHora(LocalDateTime.now())
														   .descricao("Estorno do lançamento " + lancamento.getId())
														   .natureza(natureza.getCodigo())
														   .tipoLancamento(TipoLancamento.ESTORNO.getCodigo())
														   .valor(lancamento.getValor())
														   .build();

		lancamento.setDescricao(lancamento.getDescricao() + " - Estornado");
		lancamentoRepository.save(lancamento);
		lancamentoRepository.save(lancamentoEstorno);

		Estorno estorno = Estorno.builder().lancamentoEstorno(lancamentoEstorno)
										   .lancamentoOriginal(lancamento)
										   .build();

		estornoRepository.save(estorno);

		return entidadeParaComprovanteResponseDTO(lancamentoEstorno);
	}

	private String geraAutenticacao() {
		return UUID.randomUUID().toString();
	}

	private Natureza defineNaturezaEstorno(Lancamento lancamento) {
		return Natureza.DEBITO.getCodigo().equals(lancamento.getNatureza()) ? Natureza.CREDITO : Natureza.DEBITO;
	}

}
