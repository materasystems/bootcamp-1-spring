package com.matera.bootcamp.digitalbank.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtratoResponseDTO {

	private ContaResponseDTO conta;
	private List<ComprovanteResponseDTO> lancamentos;

}
