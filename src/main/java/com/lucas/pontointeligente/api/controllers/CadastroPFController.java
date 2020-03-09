package com.lucas.pontointeligente.api.controllers;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.pontointeligente.api.dto.CadastroPFDto;
import com.lucas.pontointeligente.api.entities.Empresa;
import com.lucas.pontointeligente.api.entities.Funcionario;
import com.lucas.pontointeligente.api.enums.PerfilEnum;
import com.lucas.pontointeligente.api.response.ResponseWrapper;
import com.lucas.pontointeligente.api.services.EmpresaService;
import com.lucas.pontointeligente.api.services.FuncionarioService;
import com.lucas.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/cadastrar-pf")
@CrossOrigin(origins = "*")
public class CadastroPFController {
	
	private static final Logger log = LoggerFactory.getLogger(CadastroPFController.class);
	
	@Autowired
	private EmpresaService empresaService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	public CadastroPFController() {
		
	}
	
	
	@PostMapping
	public ResponseEntity<ResponseWrapper<CadastroPFDto>> cadastrar(@Valid @RequestBody CadastroPFDto cadastroPFDto,
			BindingResult result) throws NoSuchAlgorithmException{
		
		ResponseWrapper<CadastroPFDto> responseWrapper = new ResponseWrapper<CadastroPFDto>();
		
		validarDadosExistentes(cadastroPFDto, result);
		
		if(result.hasErrors()) {
			log.error("Erro validando dados de cadastro de PF: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> responseWrapper.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		Funcionario funcionario = this.converterDtoParaEntityFuncionario(cadastroPFDto);
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPFDto.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.persistir(funcionario);
		
		responseWrapper.setData(this.converteEntityFuncionarioEmCadastroPFDto(funcionario));
		return ResponseEntity.ok(responseWrapper);
		
		
	}
	
	private void validarDadosExistentes(CadastroPFDto cadastroPFDto, BindingResult result) {
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPFDto.getCnpj());
		
		if(!empresa.isPresent()) {
			result.addError(new ObjectError("empresa", "Empresa não cadastrada com esse CNPJ"));
			
		}
		
		this.funcionarioService.buscarPorCpf(cadastroPFDto.getCpf()).
		ifPresent(func -> result.addError(new ObjectError("funcionario", "Funcionário já cadastrado com este CPF: " + func.getCpf())));
		
		this.funcionarioService.buscarPorEmail(cadastroPFDto.getEmail()).
		ifPresent(func -> result.addError(new ObjectError("funcionario", "Funcionário já cadastrado com este email: " + func.getEmail())));
		
	}
	
	private Funcionario converterDtoParaEntityFuncionario(CadastroPFDto cadastroPFDto) throws NoSuchAlgorithmException{
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(cadastroPFDto.getNome());
		funcionario.setEmail(cadastroPFDto.getEmail());
		funcionario.setCpf(cadastroPFDto.getCpf());
		funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.gerarBcrypt(cadastroPFDto.getSenha()));
		
		cadastroPFDto.getQtdHorasAlmoco().
		ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		
		cadastroPFDto.getQtdHorasTrabalhoDia().
		ifPresent(qtdHorasTrabDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabDia)));
		
		cadastroPFDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		return funcionario;
	}
	
	private CadastroPFDto converteEntityFuncionarioEmCadastroPFDto(Funcionario funcionario) {
		CadastroPFDto cadastroPFDto = new CadastroPFDto();
		cadastroPFDto.setId(funcionario.getId());
		cadastroPFDto.setNome(funcionario.getNome());
		cadastroPFDto.setEmail(funcionario.getEmail());
		cadastroPFDto.setCnpj(funcionario.getCpf());
		cadastroPFDto.setCnpj(funcionario.getEmpresa().getCnpj());
		
		funcionario.getQtdHorasAlmocoOpt().
		ifPresent(qtdHorasAlmoco -> cadastroPFDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		
		
		funcionario.getQtdHorasTrabalhoDiaOpt().
		ifPresent(qtdHorasTrabalhoDia -> cadastroPFDto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabalhoDia))));
		
		
		funcionario.getValorHoraOpt().
		ifPresent(valorHora -> cadastroPFDto.setValorHora(Optional.of(valorHora.toString())));
		
		return cadastroPFDto;
	}
	
}
