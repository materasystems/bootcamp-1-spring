package com.matera.bootcamp.digitalbank.exception;

import java.util.Arrays;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final String codigoErro;
	private final Object[] parametros;
	
	public ServiceException(String codigoErro, Object... parametros) {
		super(codigoErro + " " + Arrays.toString(parametros));
		this.codigoErro = codigoErro;
		this.parametros = parametros;
	}

}
