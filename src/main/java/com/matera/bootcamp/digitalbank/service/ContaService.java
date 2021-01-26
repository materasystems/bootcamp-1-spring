package com.matera.bootcamp.digitalbank.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.entity.Conta;
import com.matera.bootcamp.digitalbank.enumerator.SituacaoConta;
import com.matera.bootcamp.digitalbank.exception.ServiceException;
import com.matera.bootcamp.digitalbank.repository.ContaRepository;

@Service
public class ContaService {

	@Value("${agencia.numeroMaximo:3}")
	private Integer numeroMaximoAgencia;
	
	private ContaRepository contaRepository;

	public ContaService(ContaRepository contaRepository) {
		this.contaRepository = contaRepository;
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
	    							 .orElseThrow(() -> new ServiceException("Conta não encontrada para o cliente de ID " + idCliente + "."));

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
	
	private Conta buscaPorId(Long id) {
		return contaRepository.findById(id)
							  .orElseThrow(() -> new ServiceException("Conta de ID " + id + " não encontrada."));
	}

	private void validaCadastro(Cliente cliente) {
		if (contaRepository.findByNumeroConta(cliente.getTelefone()).isPresent()) {
			throw new ServiceException(
					"Já existe uma conta com o número de telefone informado. Telefone: " + cliente.getTelefone());
		}
	}
	
    private void validaBloqueio(Conta conta) {
        if (SituacaoConta.BLOQUEADA.getCodigo().equals(conta.getSituacao())) {
            throw new ServiceException("Conta de ID " + conta.getId() + " já se encontra na situação Bloqueada.");
        }
    }
    
    private void validaDesbloqueio(Conta conta) {
        if (SituacaoConta.ABERTA.getCodigo().equals(conta.getSituacao())) {
            throw new ServiceException("Conta de ID " + conta.getId() + " já se encontra na situação Aberta.");
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
