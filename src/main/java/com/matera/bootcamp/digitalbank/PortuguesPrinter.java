package com.matera.bootcamp.digitalbank;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("pt")
public class PortuguesPrinter implements Printer {

	@Override
	public void print() {
		System.out.println("PT-BR");
	}

}
