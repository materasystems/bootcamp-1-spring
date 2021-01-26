package com.matera.bootcamp.digitalbank.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Natureza {

    CREDITO("C"),
    DEBITO("D");

    private String codigo;
    
    public static Natureza buscaPorCodigo(String codigo) {
        for (Natureza natureza : values()) {
            if (natureza.getCodigo().equals(codigo)) {
                return natureza;
            }
        }

        return null;
    }

}
