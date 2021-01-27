package com.matera.bootcamp.digitalbank.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.matera.bootcamp.digitalbank.dto.response.ErroResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ResponseDTO;
import com.matera.bootcamp.digitalbank.exception.ServiceException;

public abstract class ControllerBase {

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ResponseDTO<Object>> handleException(ServiceException exception) {
		ErroResponseDTO erro = new ErroResponseDTO(exception.getMessage());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							 .body(ResponseDTO.comErro(erro));
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseDTO<Object>> handleException(MethodArgumentNotValidException exception) {
		List<ErroResponseDTO> erros = new ArrayList<>();
		BindingResult bindingResult = exception.getBindingResult();
		
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String campo = fieldError.getField();
			String mensagem = fieldError.getDefaultMessage();
			
			erros.add(new ErroResponseDTO(campo, mensagem));
		}
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				 			 .body(ResponseDTO.comErros(erros));
	}
	
}
