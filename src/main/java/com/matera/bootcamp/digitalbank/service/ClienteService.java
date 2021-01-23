package com.matera.bootcamp.digitalbank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ClienteResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.entity.Conta;
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
	public Conta cadastra(ClienteRequestDTO clienteRequestDTO) {

		validaCadastro(clienteRequestDTO);

		Cliente cliente = clienteRequestDTOParaEntidade(clienteRequestDTO, new Cliente());

		clienteRepository.save(cliente);

		return contaService.cadastra(cliente);
	}

	public ClienteResponseDTO consulta(Long id) {

		Cliente cliente = buscaPorID(id);

		return entidadeParaClienteResponseDTO(cliente);
	}

	public List<ClienteResponseDTO> consultaTodos() {
		
		List<Cliente> clientes = clienteRepository.findAll();
		
		List<ClienteResponseDTO> clientesResponseDTO = new ArrayList<>();
		
		clientes.forEach(cli -> clientesResponseDTO.add(entidadeParaClienteResponseDTO(cli)));
		
		return clientesResponseDTO;
	}

	public void atualiza(Long id, ClienteRequestDTO clienteRequestDTO) {

		Cliente cliente = validaAtualizacao(id, clienteRequestDTO);

		Cliente clienteAtualizado = clienteRequestDTOParaEntidade(clienteRequestDTO, cliente);
		clienteRepository.save(clienteAtualizado);
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
		ClienteResponseDTO clienteResponseDTO = new ClienteResponseDTO();
		clienteResponseDTO.setId(cliente.getId());
		clienteResponseDTO.setBairro(cliente.getBairro());
		clienteResponseDTO.setCep(cliente.getCep());
		clienteResponseDTO.setCidade(cliente.getCidade());
		clienteResponseDTO.setComplemento(cliente.getComplemento());
		clienteResponseDTO.setCpf(cliente.getCpf());
		clienteResponseDTO.setEstado(cliente.getEstado());
		clienteResponseDTO.setLogradouro(cliente.getLogradouro());
		clienteResponseDTO.setNome(cliente.getNome());
		clienteResponseDTO.setNumero(cliente.getNumero());
		clienteResponseDTO.setRendaMensal(cliente.getRendaMensal());
		clienteResponseDTO.setTelefone(cliente.getTelefone());
		return clienteResponseDTO;
	}

	private Cliente buscaPorID(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cliente de ID " + id + " não encontrado."));
	}
	
	private Cliente validaAtualizacao(Long id, ClienteRequestDTO clienteRequestDTO) {
		Cliente clienteExistente = buscaPorID(id);

		if (clienteRequestDTO.getCpf().equals(clienteExistente.getCpf())
				&& !clienteExistente.getId().equals(id)) {
			throw new RuntimeException("Já existe um Cliente com esse CPF.");
		}

		return clienteExistente;
	}

	private void validaCadastro(ClienteRequestDTO clienteRequestDTO) {
		if (clienteRepository.findByCpf(clienteRequestDTO.getCpf()).isPresent()) {
			throw new RuntimeException("Já existe um Cliente com esse CPF.");
		}
	}

}
