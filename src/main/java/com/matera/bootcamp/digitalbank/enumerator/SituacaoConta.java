package com.matera.bootcamp.digitalbank.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoConta {

	ABERTA("A"),
	BLOQUEADA("B");

	private String codigo;
	
}
