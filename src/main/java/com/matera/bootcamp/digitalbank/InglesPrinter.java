package com.matera.bootcamp.digitalbank;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("en")
public class InglesPrinter implements Printer {

	@Override
	public void print() {
		System.out.println("EN-US");
	}

}
