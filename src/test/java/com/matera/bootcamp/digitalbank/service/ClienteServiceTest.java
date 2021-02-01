package com.matera.bootcamp.digitalbank.service;

import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaClienteEntidade;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaClienteRequestDTO;
import static com.matera.bootcamp.digitalbank.utils.DigitalbankTestUtils.criaContaResponseDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.matera.bootcamp.digitalbank.dto.request.ClienteRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ClienteResponseDTO;
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
        ClienteRequestDTO clienteRequestDTO = criaClienteRequestDTO();
        ContaResponseDTO contaResponseMock = criaContaResponseDTO();

        when(clienteRepository.findByCpf(eq(clienteRequestDTO.getCpf()))).thenReturn(Optional.empty());
        when(contaService.cadastra(any(Cliente.class))).thenReturn(contaResponseMock);

        ContaResponseDTO contaResponseDTO = clienteService.cadastra(clienteRequestDTO);

        verify(clienteRepository).findByCpf(eq(clienteRequestDTO.getCpf()));
        verify(clienteRepository).save(any(Cliente.class));
        verify(contaService).cadastra(any(Cliente.class));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoMoreInteractions(contaService);

        assertEquals(contaResponseMock, contaResponseDTO);
    }

    @Test
    public void cadastraClienteJaExistente() {
        ClienteRequestDTO clienteRequestDTO = criaClienteRequestDTO();
        Cliente clienteMock = criaClienteEntidade();

        when(clienteRepository.findByCpf(eq(clienteRequestDTO.getCpf()))).thenReturn(Optional.of(clienteMock));

        ServiceException serviceException = assertThrows(ServiceException.class, () -> clienteService.cadastra(clienteRequestDTO));

        verify(clienteRepository).findByCpf(eq(clienteRequestDTO.getCpf()));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);

        assertEquals("DB-2", serviceException.getCodigoErro());
    }

    @Test
    public void consultaClientePorIdComSucesso() {
        Cliente cliente = criaClienteEntidade();

        when(clienteRepository.findById(eq(cliente.getId()))).thenReturn(Optional.of(cliente));

        ClienteResponseDTO clienteResponseDTO = clienteService.consulta(cliente.getId());

        verify(clienteRepository).findById(eq(cliente.getId()));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);

        assertNotNull(clienteResponseDTO);
        assertEquals(cliente.getId(), clienteResponseDTO.getId());
    }

    @Test
    public void consultaClientePorIdNaoExistente() {
        Long idCliente = 1L;

        when(clienteRepository.findById(eq(idCliente))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> clienteService.consulta(idCliente));

        verify(clienteRepository).findById(eq(idCliente));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);

        assertEquals("DB-1", serviceException.getCodigoErro());
    }

    @Test
    public void consultaTodosOsClientes() {
        List<Cliente> clientesMock = Arrays.asList(criaClienteEntidade(), criaClienteEntidade());

        when(clienteRepository.findAll()).thenReturn(clientesMock);

        List<ClienteResponseDTO> clientesResponse = clienteService.consultaTodos();

        verify(clienteRepository).findAll();
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);

        assertEquals(clientesMock.size(), clientesResponse.size());
    }

    @Test
    public void atualizaClienteComSucesso() {
        ClienteRequestDTO clienteRequest = criaClienteRequestDTO();
        Cliente clienteMock = criaClienteEntidade();

        when(clienteRepository.findByCpf(eq(clienteRequest.getCpf()))).thenReturn(Optional.empty());
        when(clienteRepository.findById(eq(clienteMock.getId()))).thenReturn(Optional.of(clienteMock));

        clienteService.atualiza(clienteMock.getId(), clienteRequest);

        verify(clienteRepository).findByCpf(eq(clienteRequest.getCpf()));
        verify(clienteRepository).findById(eq(clienteMock.getId()));
        verify(clienteRepository).save(any(Cliente.class));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);
    }

    @Test
    public void atualizaClienteCpfJaExistente() {
        ClienteRequestDTO clienteRequest = criaClienteRequestDTO();
        Long idCliente = 1L;
        Cliente clienteJaExistente = criaClienteEntidade();
        clienteJaExistente.setId(2L);

        when(clienteRepository.findByCpf(eq(clienteRequest.getCpf()))).thenReturn(Optional.of(clienteJaExistente));

        ServiceException serviceException = assertThrows(ServiceException.class, () -> clienteService.atualiza(idCliente, clienteRequest));

        verify(clienteRepository).findByCpf(eq(clienteRequest.getCpf()));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);

        assertEquals("DB-2", serviceException.getCodigoErro());
    }

    @Test
    public void atualizaClienteNaoExistente() {
        Long idCliente = 1L;
        ClienteRequestDTO clienteRequest = criaClienteRequestDTO();

        when(clienteRepository.findByCpf(eq(clienteRequest.getCpf()))).thenReturn(Optional.empty());
        when(clienteRepository.findById(eq(idCliente))).thenReturn(Optional.empty());

        ServiceException serviceException = assertThrows(ServiceException.class, () -> clienteService.atualiza(idCliente, clienteRequest));

        verify(clienteRepository).findByCpf(eq(clienteRequest.getCpf()));
        verify(clienteRepository).findById(eq(idCliente));
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(contaService);

        assertEquals("DB-1", serviceException.getCodigoErro());
    }

    @Test
    public void consultaContaPorIdCliente() {
        Long idCliente = 1L;
        ContaResponseDTO contaResponseMock = criaContaResponseDTO();

        when(contaService.consultaContaPorIdCliente(eq(idCliente))).thenReturn(contaResponseMock);

        ContaResponseDTO contaResponse = clienteService.consultaContaPorIdCliente(idCliente);

        verify(contaService).consultaContaPorIdCliente(eq(idCliente));
        verifyNoMoreInteractions(contaService);
        verifyNoInteractions(clienteRepository);

        assertNotNull(contaResponse);
    }

}
