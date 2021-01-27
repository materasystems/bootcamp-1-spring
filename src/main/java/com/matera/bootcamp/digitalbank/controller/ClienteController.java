package com.matera.bootcamp.digitalbank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ClienteResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ResponseDTO;
import com.matera.bootcamp.digitalbank.service.ClienteService;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController extends ControllerBase {

	private ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@PostMapping
	public ResponseEntity<ResponseDTO<ContaResponseDTO>> cadastra(@RequestBody ClienteRequestDTO clienteRequestDTO) {
		ContaResponseDTO contaResponseDTO = clienteService.cadastra(clienteRequestDTO);

		return ResponseEntity.status(HttpStatus.CREATED)
							 .body(new ResponseDTO<>(contaResponseDTO));
	}

	@GetMapping
	public ResponseEntity<ResponseDTO<List<ClienteResponseDTO>>> consultaTodos() {

		List<ClienteResponseDTO> clientes = clienteService.consultaTodos();

		return ResponseEntity.ok(new ResponseDTO<>(clientes));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseDTO<ClienteResponseDTO>> consultaPorId(@PathVariable Long id) {

		ClienteResponseDTO clienteResponseDTO = clienteService.consulta(id);

		return ResponseEntity.ok(new ResponseDTO<>(clienteResponseDTO));
	}

	@GetMapping("/{id}/conta")
	public ResponseEntity<ResponseDTO<ContaResponseDTO>> consultaContaPorIdCliente(@PathVariable Long id) {

		ContaResponseDTO contaResponseDTO = clienteService.consultaContaPorIdCliente(id);

		return ResponseEntity.ok(new ResponseDTO<>(contaResponseDTO));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseDTO<Void>> atualiza(@PathVariable Long id, @RequestBody ClienteRequestDTO clienteRequestDTO) {

		clienteService.atualiza(id, clienteRequestDTO);

		return ResponseEntity.noContent().build();
	}

}
