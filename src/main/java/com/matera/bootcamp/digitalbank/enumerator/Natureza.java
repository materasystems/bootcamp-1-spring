package com.matera.bootcamp.digitalbank.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Natureza {

    CREDITO("C"),
    DEBITO("D");

    private String codigo;

}
