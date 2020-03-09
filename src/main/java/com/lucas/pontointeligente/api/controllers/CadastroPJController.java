package com.lucas.pontointeligente.api.controllers;

import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.pontointeligente.api.dto.CadastroPJDto;
import com.lucas.pontointeligente.api.entities.Empresa;
import com.lucas.pontointeligente.api.entities.Funcionario;
import com.lucas.pontointeligente.api.enums.PerfilEnum;
import com.lucas.pontointeligente.api.response.ResponseWrapper;
import com.lucas.pontointeligente.api.services.EmpresaService;
import com.lucas.pontointeligente.api.services.FuncionarioService;
import com.lucas.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/cadastrar-pj")
@CrossOrigin(origins = "*")
public class CadastroPJController {
	
	private static final Logger log = LoggerFactory.getLogger(CadastroPJController.class);
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	@Autowired
	private EmpresaService empresaService;
	
	public CadastroPJController() {
		
	}
	
	@PostMapping
	public ResponseEntity<ResponseWrapper<CadastroPJDto>> cadastrar(@Valid @RequestBody CadastroPJDto cadastroPJDto,
			BindingResult result) throws NoSuchAlgorithmException{
		
		log.info("Cadastrando PJ: {}", cadastroPJDto);
		
		
		ResponseWrapper<CadastroPJDto> responseWrapper = new ResponseWrapper<CadastroPJDto>();
		
		//validarDadosExistentes(cadastroPJDto, result);
		
		if(result.hasErrors()) {
			log.error("Erro Validando dados de cadastro PJ: {}", result.getAllErrors());
			
			result.getAllErrors().forEach(error -> responseWrapper.getErrors().add(error.getDefaultMessage()));
			
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		
		Empresa empresa = this.converterDtoParaEntityEmpresa(cadastroPJDto); //converte DTO para Entity
		Funcionario funcionario = this.converterDtoParaEntityFuncionario(cadastroPJDto); //converte DTO para Entity
		funcionario.setEmpresa(empresa);
		
		log.info("------->>>> Funcionário antes de persistir: {}", funcionario);
		
		this.empresaService.persistir(empresa); //persistindo Entity Empresa
		this.funcionarioService.persistir(funcionario); //persistindo Entity Empresa
		
		log.info("------->>>> Funcionário d de persistir: {}", funcionario);
		
		responseWrapper.setData(this.converterFuncionarioEntityEmCadastroPJDto(funcionario));
				
		return ResponseEntity.ok(responseWrapper);
		
	}
	
	private void validarDadosExistentes(CadastroPJDto cadastroPJDto, BindingResult result) {
		
		//Verifica várias existências e as adiciona no result
		
		//busca se já existe o CNPJ na base
		this.empresaService.buscarPorCnpj(cadastroPJDto.getCnpj()).ifPresent(
				emp -> result.addError(new ObjectError("empresa", "Empresa já existente.")));
		
		//busca se já existe o CPF na base
		this.funcionarioService.buscarPorCpf(cadastroPJDto.getCpf()).ifPresent(
				emp -> result.addError(new ObjectError("funcionario", "Esse CPF já existe na base")));
		
		//busca se já existe o email na base
		this.funcionarioService.buscarPorEmail(cadastroPJDto.getEmail()).ifPresent(
				emp -> result.addError(new ObjectError("funcionario", "Esse email já existe na base")));
		
	}
	
	private Empresa converterDtoParaEntityEmpresa(CadastroPJDto cadastroPJDto) {
		Empresa empresa = new Empresa();
		
		empresa.setCnpj(cadastroPJDto.getCnpj());
		empresa.setRazaoSocial(cadastroPJDto.getRazaoSocial());
		
		return empresa;
	}
	
	private Funcionario converterDtoParaEntityFuncionario(CadastroPJDto cadastroPJDto) throws NoSuchAlgorithmException{
		
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(cadastroPJDto.getNome());
		funcionario.setEmail(cadastroPJDto.getEmail());
		funcionario.setCpf(cadastroPJDto.getCpf());
		funcionario.setPerfil(PerfilEnum.ROLE_ADMIN);
		funcionario.setSenha(PasswordUtils.gerarBcrypt(cadastroPJDto.getSenha()));
		
		return funcionario;
		
	}
	
	private CadastroPJDto converterFuncionarioEntityEmCadastroPJDto(Funcionario funcionario) {
		CadastroPJDto cadastroPJDto = new CadastroPJDto();
		
		cadastroPJDto.setId(funcionario.getId());
		cadastroPJDto.setNome(funcionario.getNome());
		cadastroPJDto.setEmail(funcionario.getEmail());
		cadastroPJDto.setCpf(funcionario.getCpf());
		cadastroPJDto.setSenha(funcionario.getSenha());
		cadastroPJDto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
		cadastroPJDto.setCnpj(funcionario.getEmpresa().getCnpj());
		
		return cadastroPJDto;
	}
	
	 @GetMapping("/testRest")
	public void testRest() {
		 log.info("---------------->>>> BATEU NO REST <<<<<--------------");
		
	}
	
	

}
