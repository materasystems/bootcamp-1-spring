package com.matera.bootcamp.digitalbank.service;

import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaClienteRequestDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaContaResponseDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.entity.Cliente;
import com.matera.bootcamp.digitalbank.exception.ServiceException;
import com.matera.bootcamp.digitalbank.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private ContaService contaService;

	@InjectMocks
	private ClienteService clienteService;

	@Test
	public void cadastraClienteComSucesso() {

		ClienteRequestDTO clienteRequestDTOMock = criaClienteRequestDTO();
		ContaResponseDTO contaResponseDTOMock = criaContaResponseDTO();

		when(clienteRepository.findByCpf(clienteRequestDTOMock.getCpf())).thenReturn(Optional.empty());
		when(contaService.cadastra(any(Cliente.class))).thenReturn(contaResponseDTOMock);

		ContaResponseDTO contaResponseDTO = clienteService.cadastra(clienteRequestDTOMock);

		verify(clienteRepository).findByCpf(clienteRequestDTOMock.getCpf());
		verify(clienteRepository).save(any(Cliente.class));
		verify(contaService).cadastra(any(Cliente.class));
		verifyNoMoreInteractions(clienteRepository);
		verifyNoMoreInteractions(contaService);

		assertEquals(contaResponseDTOMock, contaResponseDTO);
	}

	@Test
	public void cadastraClienteComErroDB2() {

		ClienteRequestDTO clienteRequestDTOMock = criaClienteRequestDTO();

		when(clienteRepository.findByCpf(clienteRequestDTOMock.getCpf())).thenReturn(Optional.of(new Cliente()));

		ServiceException exception = assertThrows(ServiceException.class, () -> clienteService.cadastra(clienteRequestDTOMock));
		
		verify(clienteRepository).findByCpf(clienteRequestDTOMock.getCpf());
		verifyNoMoreInteractions(clienteRepository);
		verifyNoInteractions(contaService);

		assertEquals("DB-2", exception.getCodigoErro());
	}

}

