package com.matera.bootcamp.digitalbank.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matera.bootcamp.digitalbank.dto.request.LancamentoRequestDTO;
import com.matera.bootcamp.digitalbank.dto.request.TransferenciaRequestDTO;
import com.matera.bootcamp.digitalbank.dto.response.ComprovanteResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ContaResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ExtratoResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ResponseDTO;
import com.matera.bootcamp.digitalbank.enumerator.TipoLancamento;
import com.matera.bootcamp.digitalbank.service.ContaService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/contas")
@Slf4j
public class ContaController extends ControllerBase {

    private final ContaService contaService;

    public ContaController(ContaService contaService, MessageSource messageSource) {
    	super(messageSource);
        this.contaService = contaService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<ContaResponseDTO>>> consultaTodas() {
        List<ContaResponseDTO> contasResponseDTO = contaService.consultaTodas();

        return ResponseEntity.ok(new ResponseDTO<>(contasResponseDTO));
    }

    @PostMapping("/{id}/depositar")
    public ResponseEntity<ResponseDTO<ComprovanteResponseDTO>> efetuaDeposito(@PathVariable("id") Long id, @Valid @RequestBody LancamentoRequestDTO lancamentoRequestDTO) {
    	log.debug("Iniciando POST em /api/v1/contas/{id}/depositar com id {} e request {}", id, lancamentoRequestDTO);
    	
        ComprovanteResponseDTO comprovanteResponseDTO = contaService.efetuaLancamento(id, lancamentoRequestDTO, TipoLancamento.DEPOSITO);
        
        log.debug("Finalizando POST em /api/v1/contas/{id}/depositar com response {}", comprovanteResponseDTO);

        return ResponseEntity.ok(new ResponseDTO<>(comprovanteResponseDTO));
    }

    @PostMapping("/{id}/sacar")
    public ResponseEntity<ResponseDTO<ComprovanteResponseDTO>> efetuaSaque(@PathVariable("id") Long id, @Valid @RequestBody LancamentoRequestDTO lancamentoRequestDTO) {
    	log.debug("Iniciando POST em /api/v1/contas/{id}/sacar com id {} e request {}", id, lancamentoRequestDTO);
    	
        ComprovanteResponseDTO comprovanteResponseDTO = contaService.efetuaLancamento(id, lancamentoRequestDTO, TipoLancamento.SAQUE);
        
        log.debug("Finalizando POST em /api/v1/contas/{id}/sacar com response {}", comprovanteResponseDTO);

        return ResponseEntity.ok(new ResponseDTO<>(comprovanteResponseDTO));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<ResponseDTO<ComprovanteResponseDTO>> efetuaPagamento(@PathVariable("id") Long id, @Valid @RequestBody LancamentoRequestDTO lancamentoRequestDTO) {
        log.debug("Iniciando POST em /api/v1/contas/{id}/pagar com id {} e request {}", id, lancamentoRequestDTO);

        ComprovanteResponseDTO comprovanteResponseDTO = contaService.efetuaLancamento(id, lancamentoRequestDTO, TipoLancamento.PAGAMENTO);

        log.debug("Finalizando POST em /api/v1/contas/{id}/pagar com response {}", comprovanteResponseDTO);

        return ResponseEntity.ok(new ResponseDTO<>(comprovanteResponseDTO));
    }

    @PostMapping("/{id}/transferir")
    public ResponseEntity<ResponseDTO<ComprovanteResponseDTO>> efetuaTransferencia(@PathVariable("id") Long id, @Valid @RequestBody TransferenciaRequestDTO transferenciaRequestDTO) {
        log.debug("Iniciando POST em /api/v1/contas/{id}/transferir com id {} e request {}", id, transferenciaRequestDTO);

    	ComprovanteResponseDTO comprovanteResponseDTO = contaService.efetuaTransferencia(id, transferenciaRequestDTO);
    	
        log.debug("Finalizando POST em /api/v1/contas/{id}/transferir com response {}", comprovanteResponseDTO);

        return ResponseEntity.ok(new ResponseDTO<>(comprovanteResponseDTO));
    }

    @GetMapping(value = "/{id}/lancamentos", params = { "!dataInicial", "!dataFinal" })
    public ResponseEntity<ResponseDTO<ExtratoResponseDTO>> consultaExtratoCompleto(@PathVariable("id") Long id) {
        log.debug("Iniciando GET em /api/v1/contas/{id}/lancamentos com id {}", id);

        ExtratoResponseDTO extratoResponseDTO = contaService.consultaExtratoCompleto(id);

        log.debug("Finalizando GET em /api/v1/contas/{id}/lancamentos com response {}", extratoResponseDTO);

        return ResponseEntity.ok(new ResponseDTO<>(extratoResponseDTO));
    }

    @GetMapping(value = "/{id}/lancamentos", params = { "dataInicial", "dataFinal" })
    public ResponseEntity<ResponseDTO<ExtratoResponseDTO>> consultaExtratoPorPeriodo(@PathVariable("id") Long id,
                                                                                     @RequestParam(value = "dataInicial", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dataInicial,
                                                                                     @RequestParam(value = "dataFinal", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dataFinal) {
    	log.debug("Iniciando GET em /api/v1/contas/{id}/lancamentos com id {}, dataInicial {} e dataFinal {}", id, dataInicial, dataFinal);
    	
    	ExtratoResponseDTO extratoResponseDTO = contaService.consultaExtratoPorPeriodo(id, dataInicial, dataFinal);
    	
        log.debug("Finalizando GET em /api/v1/contas/{id}/lancamentos com response {}", extratoResponseDTO);

    	return ResponseEntity.ok(new ResponseDTO<>(extratoResponseDTO));
    }

    @GetMapping("/{idConta}/lancamentos/{idLancamento}")
    public ResponseEntity<ResponseDTO<ComprovanteResponseDTO>> consultaComprovanteLancamento(@PathVariable("idConta") Long idConta,
                                                                                             @PathVariable("idLancamento") Long idLancamento) {
        log.debug("Iniciando GET em /api/v1/contas/{idConta}/lancamentos/{idLancamento} com idConta {} e idLancamento {}", idConta, idLancamento);
        
    	ComprovanteResponseDTO comprovanteResponseDTO = contaService.consultaComprovanteLancamento(idConta, idLancamento);
    	
        log.debug("Finalizando GET em /api/v1/contas/{idConta}/lancamentos/{idLancamento} com response {}", comprovanteResponseDTO);

        return ResponseEntity.ok(new ResponseDTO<>(comprovanteResponseDTO));
    }

    @PostMapping("/{idConta}/lancamentos/{idLancamento}/estornar")
    public ResponseEntity<ResponseDTO<ComprovanteResponseDTO>> estornaLancamento(@PathVariable("idConta") Long idConta,
                                                                                 @PathVariable("idLancamento") Long idLancamento) {
        log.debug("Iniciando POST em /api/v1/contas/{idConta}/lancamentos/{idLancamento}/estornar com idConta {} e idLancamento {}", idConta, idLancamento);

        ComprovanteResponseDTO comprovanteResponseDTO = contaService.estornaLancamento(idConta, idLancamento);

        log.debug("Finalizando POST em /api/v1/contas/{idConta}/lancamentos/{idLancamento}/estornar com response {}", comprovanteResponseDTO);
        
        return ResponseEntity.ok(new ResponseDTO<>(comprovanteResponseDTO));
    }

    @DeleteMapping("/{idConta}/lancamentos/{idLancamento}")
    public ResponseEntity<Void> removeLancamentoEstorno(@PathVariable("idConta") Long idConta,
                                                        @PathVariable("idLancamento") Long idLancamento) {
        log.debug("Iniciando DELETE em /api/v1/contas/{idConta}/lancamentos/{idLancamento} com idConta {} e idLancamento {}", idConta, idLancamento);

        contaService.removeLancamentoEstorno(idConta, idLancamento);

        log.debug("Finalizando DELETE em /api/v1/contas/{idConta}/lancamentos/{idLancamento}");

        return ResponseEntity.noContent()
                             .build();
    }

    @PostMapping("/{id}/bloquear")
    public ResponseEntity<Void> bloqueia(@PathVariable("id") Long id) {
        log.debug("Iniciando POST em /api/v1/contas/{id}/bloquear com id {}", id);

        contaService.bloqueiaConta(id);

        log.debug("Finalizando POST em /api/v1/contas/{id}/bloquear");

        return ResponseEntity.noContent()
                             .build();
    }

    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<Void> desbloqueia(@PathVariable("id") Long id) {
        log.debug("Iniciando POST em /api/v1/contas/{id}/desbloquear com id {}", id);

        contaService.desbloqueiaConta(id);

        log.debug("Finalizando POST em /api/v1/contas/{id}/desbloquear");

        return ResponseEntity.noContent()
                             .build();
    }

}
