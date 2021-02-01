package com.matera.bootcamp.digitalbank.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.springframework.context.MessageSource;
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

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/clientes")
@Slf4j
public class ClienteController extends ControllerBase {

	private ClienteService clienteService;

	public ClienteController(ClienteService clienteService, MessageSource messageSource) {
		super(messageSource);
		this.clienteService = clienteService;
	}

	@PostMapping
	public ResponseEntity<ResponseDTO<ContaResponseDTO>> cadastra(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
		log.debug("Iniciando POST em /api/v1/clientes com request {}", clienteRequestDTO);
		
		ContaResponseDTO contaResponseDTO = clienteService.cadastra(clienteRequestDTO);
		
		log.debug("Finalizando POST em /api/v1/clientes com response {}", contaResponseDTO);

		return ResponseEntity.status(HttpStatus.CREATED)
							 .body(new ResponseDTO<>(contaResponseDTO));
	}

	@GetMapping
	public ResponseEntity<ResponseDTO<List<ClienteResponseDTO>>> consultaTodos() {
		log.debug("Iniciando GET em /api/v1/clientes");

		List<ClienteResponseDTO> clientesResponseDTO = clienteService.consultaTodos();
		
		log.debug("Finalizando GET em /api/v1/clientes com response {}", Arrays.toString(clientesResponseDTO.toArray()));

		return ResponseEntity.ok(new ResponseDTO<>(clientesResponseDTO));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseDTO<ClienteResponseDTO>> consultaPorId(@PathVariable Long id) {
		log.debug("Iniciando GET em /api/v1/clientes/{id} com id {}", id);

		ClienteResponseDTO clienteResponseDTO = clienteService.consulta(id);
		
		log.debug("Finalizando GET em /api/v1/clientes/{id} com response {}", clienteResponseDTO);

		return ResponseEntity.ok(new ResponseDTO<>(clienteResponseDTO));
	}

	@GetMapping("/{id}/conta")
	public ResponseEntity<ResponseDTO<ContaResponseDTO>> consultaContaPorIdCliente(@PathVariable Long id) {
		log.debug("Iniciando GET em /api/v1/clientes/{id}/conta com id {}", id);

		ContaResponseDTO contaResponseDTO = clienteService.consultaContaPorIdCliente(id);
		
		log.debug("Finalizando GET em /api/v1/clientes/{id}/conta com response {}", contaResponseDTO);

		return ResponseEntity.ok(new ResponseDTO<>(contaResponseDTO));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseDTO<Void>> atualiza(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
		log.debug("Iniciando PUT em /api/v1/clientes/{id} com id {} e request {}", id, clienteRequestDTO);

		clienteService.atualiza(id, clienteRequestDTO);
		
		log.debug("Finalizando PUT em /api/v1/clientes/{id}");

		return ResponseEntity.noContent().build();
	}

}
