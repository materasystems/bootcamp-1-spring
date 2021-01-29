package com.matera.bootcamp.digitalbank.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ClienteResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.exception.ServiceException;
import com.matera.bootcamp.digitalbank.repository.ClienteRepository;

@Service
public class ClienteService {

	private ClienteRepository clienteRepository;
	private ContaService contaService;

	public ClienteService(ClienteRepository clienteRepository, ContaService contaService) {
		this.clienteRepository = clienteRepository;
		this.contaService = contaService;
	}

	@Transactional
	public ContaResponseDTO cadastra(ClienteRequestDTO clienteRequestDTO) {
		validaCadastro(clienteRequestDTO);

		Cliente cliente = clienteRequestDTOParaEntidade(clienteRequestDTO, new Cliente());

		clienteRepository.save(cliente);

		return contaService.cadastra(cliente);
	}

	public ClienteResponseDTO consulta(Long id) {
		Cliente cliente = buscaPorId(id);

		return entidadeParaClienteResponseDTO(cliente);
	}

	public List<ClienteResponseDTO> consultaTodos() {
		List<Cliente> clientes = clienteRepository.findAll();
		List<ClienteResponseDTO> clientesResponseDTO = new ArrayList<>();
		
		clientes.forEach(cli -> clientesResponseDTO.add(entidadeParaClienteResponseDTO(cli)));
		
		return clientesResponseDTO;
	}
	
	public ContaResponseDTO consultaContaPorIdCliente(Long idCliente) {
		return contaService.consultaContaPorIdCliente(idCliente);
	}

	@Transactional
	public void atualiza(Long id, ClienteRequestDTO clienteRequestDTO) {
		validaAtualizacao(id, clienteRequestDTO);

		Cliente clienteAtualizado = clienteRequestDTOParaEntidade(clienteRequestDTO, buscaPorId(id));
		
		clienteRepository.save(clienteAtualizado);
	}

	private Cliente buscaPorId(Long id) {
		return clienteRepository.findById(id).orElseThrow(() -> new ServiceException("DB-1", id));
	}
	
	private void validaCadastro(ClienteRequestDTO clienteRequestDTO) {
		if (clienteRepository.findByCpf(clienteRequestDTO.getCpf()).isPresent()) {
			throw new ServiceException("DB-2", clienteRequestDTO.getCpf());
		}
	}
	
	private void validaAtualizacao(Long id, ClienteRequestDTO clienteRequestDTO) {
		Optional<Cliente> clienteExistente = clienteRepository.findByCpf(clienteRequestDTO.getCpf());

		if (clienteExistente.isPresent() && !clienteExistente.get().getId().equals(id)) {
			throw new ServiceException("DB-2", clienteRequestDTO.getCpf());
		}
	}
	
	private Cliente clienteRequestDTOParaEntidade(ClienteRequestDTO clienteRequestDTO, Cliente cliente) {
		cliente.setBairro(clienteRequestDTO.getBairro());
		cliente.setCep(clienteRequestDTO.getCep());
		cliente.setCidade(clienteRequestDTO.getCidade());
		cliente.setComplemento(clienteRequestDTO.getComplemento());
		cliente.setCpf(clienteRequestDTO.getCpf());
		cliente.setEstado(clienteRequestDTO.getEstado());
		cliente.setLogradouro(clienteRequestDTO.getLogradouro());
		cliente.setNome(clienteRequestDTO.getNome());
		cliente.setNumero(clienteRequestDTO.getNumero());
		cliente.setRendaMensal(clienteRequestDTO.getRendaMensal());
		cliente.setTelefone(clienteRequestDTO.getTelefone());
		
		return cliente;
	}

	private ClienteResponseDTO entidadeParaClienteResponseDTO(Cliente cliente) {
		return ClienteResponseDTO.builder().id(cliente.getId())
										   .bairro(cliente.getBairro())
										   .cep(cliente.getCep())
										   .cidade(cliente.getCidade())
										   .complemento(cliente.getComplemento())
										   .cpf(cliente.getCpf())
										   .estado(cliente.getEstado())
										   .logradouro(cliente.getLogradouro())
										   .nome(cliente.getNome())
										   .numero(cliente.getNumero())
										   .rendaMensal(cliente.getRendaMensal())
										   .telefone(cliente.getTelefone())
										   .build();
	}

}
