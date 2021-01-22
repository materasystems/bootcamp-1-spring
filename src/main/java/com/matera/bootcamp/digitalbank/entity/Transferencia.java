package com.matera.bootcamp.digitalbank.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "db_transferencia")
public class Transferencia extends EntidadeBase {

    @OneToOne
	@JoinColumn(name = "id_lancamento_debito", nullable = false)
	private Lancamento lancamentoDebito;

    @OneToOne
	@JoinColumn(name = "id_lancamento_credito", nullable = false)
    private Lancamento lancamentoCredito;

}
