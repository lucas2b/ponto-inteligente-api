package com.lucas.pontointeligente.api.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.pontointeligente.api.dto.EmpresaDto;
import com.lucas.pontointeligente.api.entities.Empresa;
import com.lucas.pontointeligente.api.response.ResponseWrapper;
import com.lucas.pontointeligente.api.services.EmpresaService;

@RestController
@RequestMapping("api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {
	
	private static final Logger log = LoggerFactory.getLogger(EmpresaController.class);
	
	@Autowired
	private EmpresaService empresaService;
	
	public EmpresaController() {
		
	}
	
	@GetMapping(value = "/cnpj/{cnpj}")
	public ResponseEntity<ResponseWrapper<EmpresaDto>> buscarPorCnpj(@PathVariable("cnpj") String cnpj){
		log.info("Bucando empresa por CNPJ: ", cnpj);
		ResponseWrapper<EmpresaDto> responseWrapper = new ResponseWrapper<EmpresaDto>();
		
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cnpj);
		
		if(!empresa.isPresent()) {
			log.info("Empresa não encontrada para o cnpj: {}", cnpj);
			responseWrapper.getErrors().add("Empresa não encontrada para o CNPJ: " + cnpj);
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		responseWrapper.setData(this.converterEntityEmpresaEmEmpresaDto(empresa.get()));
		return ResponseEntity.ok(responseWrapper);

		
	}
	
	
	private EmpresaDto converterEntityEmpresaEmEmpresaDto(Empresa empresa) {
		EmpresaDto empresaDto = new EmpresaDto();
		empresaDto.setId(empresa.getId());
		empresaDto.setCnpj(empresa.getCnpj());
		empresaDto.setDataCriacao(empresa.getDataCriacao());
		empresaDto.setRazaoSocial(empresa.getRazaoSocial());
		
		return empresaDto;
	}

}
