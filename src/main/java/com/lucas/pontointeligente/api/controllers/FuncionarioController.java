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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.pontointeligente.api.dto.FuncionarioDto;
import com.lucas.pontointeligente.api.entities.Funcionario;
import com.lucas.pontointeligente.api.response.ResponseWrapper;
import com.lucas.pontointeligente.api.services.FuncionarioService;
import com.lucas.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionarioController {
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioController.class);
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	
	/**
	 * Atualiza o dados de um funcionário.
	 * 
	 * @param id
	 * @param funcionarioDto
	 * @param result
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<ResponseWrapper<FuncionarioDto>> atualizar(@PathVariable("id") Long id,
			@Valid @RequestBody FuncionarioDto funcionarioDto, BindingResult result) throws NoSuchAlgorithmException{
		
		ResponseWrapper<FuncionarioDto> responseWrapper = new ResponseWrapper<FuncionarioDto>();
		
		log.info("Atulizando funcionário: {}", funcionarioDto.toString());
		
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(id);
		
		if(!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não encontrado na base de dados"));
		}
		
		this.atualizarDadosFuncionario(funcionario.get(), funcionarioDto, result);
		
		if(result.hasErrors()) {
			log.error("Erro validando dados do Funcionário: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> responseWrapper.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		this.funcionarioService.persistir(funcionario.get());
		responseWrapper.setData(this.converterEntityFuncionarioEmFuncionarioDto(funcionario.get()));
		
		return ResponseEntity.ok(responseWrapper);
	}
	
	/**
	 * Atualiza os dados do funcionário com base nos dados encontrados no DTO.
	 * 
	 * @param funcionario
	 * @param funcionarioDto
	 * @param result
	 * @throws NoSuchAlgorithmException
	 */
	private void atualizarDadosFuncionario(Funcionario funcionario, FuncionarioDto funcionarioDto, BindingResult result)
			throws NoSuchAlgorithmException{
		
		funcionario.setNome(funcionarioDto.getNome());
		
		if(!funcionario.getEmail().equals(funcionarioDto.getEmail())) { //email da tela é diferente da base de dados
			
			//busca se o email da tela ja está cadastrado, caso tiver, adiciona um erro
			this.funcionarioService.buscarPorEmail(funcionarioDto.getEmail()).
			ifPresent(param -> result.addError(new ObjectError("email", "Email já cadastrado.")));
			
			funcionario.setEmail(funcionarioDto.getEmail());
		}
		
		funcionario.setQtdHorasAlmoco(null);
		funcionarioDto.getQtdHorasTrabalhoDia()
		.ifPresent(qtdHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalhoDia)));
		
		funcionarioDto.getQtdHorasTrabalhoDia().
		ifPresent(qtdHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalhoDia)));
		
		funcionario.setValorHora(null);
		funcionarioDto.getValorHora().
		ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		if(funcionarioDto.getSenha().isPresent()) {
			funcionario.setSenha(PasswordUtils.gerarBcrypt(funcionarioDto.getSenha().get()));
		}
		
	}
	
	private FuncionarioDto converterEntityFuncionarioEmFuncionarioDto(Funcionario funcionario) {
			
			FuncionarioDto funcionarioDto = new FuncionarioDto();
			funcionarioDto.setId(funcionario.getId());
			funcionarioDto.setEmail(funcionario.getEmail());
			funcionarioDto.setNome(funcionario.getNome());
			
			funcionario.getQtdHorasAlmocoOpt().ifPresent(
					qtdHorasAlmoco -> funcionarioDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
			
			funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(
					qtdHorasTrabalhoDia -> funcionarioDto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabalhoDia))));
			
			funcionario.getValorHoraOpt().ifPresent(
					valorHora -> funcionarioDto.setValorHora(Optional.of(valorHora.toString())));
			
			return funcionarioDto;
	}

}
