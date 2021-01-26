package com.matera.bootcamp.digitalbank.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoLancamento {

    DEPOSITO("D"),
    SAQUE("S"),
    TRANSFERENCIA("T"),
    PAGAMENTO("P"),
    ESTORNO("E");

    private String codigo;

}
