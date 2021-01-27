package com.matera.bootcamp.digitalbank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.matera.bootcamp.digitalbank.dto.response.ErroResponseDTO;
import com.matera.bootcamp.digitalbank.dto.response.ResponseDTO;
import com.matera.bootcamp.digitalbank.exception.ServiceException;

public abstract class ControllerBase {

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ResponseDTO<Object>> handleException(ServiceException exception){
		ErroResponseDTO erro = new ErroResponseDTO(exception.getMessage());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							 .body(ResponseDTO.comErro(erro));
	}
	
}
