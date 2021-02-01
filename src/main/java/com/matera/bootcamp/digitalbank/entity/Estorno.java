package com.matera.bootcamp.digitalbank.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
@Entity
@Table(name = "db_estorno")
public class Estorno extends EntidadeBase {

	@OneToOne
	@JoinColumn(name = "id_lancamento_original", nullable = false)
	private Lancamento lancamentoOriginal;

	@OneToOne
	@JoinColumn(name = "id_lancamento_estorno", nullable = false)
	private Lancamento lancamentoEstorno;
	
}
