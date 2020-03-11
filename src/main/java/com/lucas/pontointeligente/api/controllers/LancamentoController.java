package com.lucas.pontointeligente.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.pontointeligente.api.dto.LancamentoDto;
import com.lucas.pontointeligente.api.entities.Funcionario;
import com.lucas.pontointeligente.api.entities.Lancamento;
import com.lucas.pontointeligente.api.enums.TipoEnum;
import com.lucas.pontointeligente.api.response.ResponseWrapper;
import com.lucas.pontointeligente.api.services.FuncionarioService;
import com.lucas.pontointeligente.api.services.LancamentoService;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {
	
	private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	@Value("${paginacao.qdt_por_pagina}")
	private int qtdPorPagina;
	
	public LancamentoController() {
		
	}
	
	/**
	 * Retorna a listagem de lançamentos de um funcionário
	 * 
	 * @param funcionarioId
	 * @param pag
	 * @param ord
	 * @param dir
	 * @return
	 */
	@GetMapping(value = "/funcionario/{funcionarioId}")
	public ResponseEntity<ResponseWrapper<Page<LancamentoDto>>> listarLancamentoPorFuncionarioId(
			@PathVariable("funcionarioId") Long funcionarioId,
			@RequestParam(value = "pag", defaultValue = "0") int pag,
			@RequestParam(value = "ord", defaultValue = "id") String ord,
			@RequestParam(value = "dir", defaultValue = "DESC") String dir){
		
		log.info("Buscando lançamentos por ID do funcionário: {}, página: {}", funcionarioId, pag);
		ResponseWrapper<Page<LancamentoDto>> responseWrapper = new ResponseWrapper<Page<LancamentoDto>>();
		
		PageRequest pageRequest = PageRequest.of(pag, this.qtdPorPagina, Direction.valueOf(dir), ord);
		Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(funcionarioId, pageRequest);
		Page<LancamentoDto> lancamentosDto = lancamentos.map(lancamento -> this.converterEntityLancamentoEmLancamentoDto(lancamento));
		
		responseWrapper.setData(lancamentosDto);
		return ResponseEntity.ok(responseWrapper);
		
		
	}
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<ResponseWrapper<LancamentoDto>> listarLancamentoPorIdDeLancamento(@PathVariable("id") Long id){
		
		log.info("Buscando lançamento por ID: {}", id);
		ResponseWrapper<LancamentoDto> responseWrapper = new ResponseWrapper<LancamentoDto>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if(!lancamento.isPresent()) {
			log.info("Lançamento não encontrado para o ID: {}", id);
			responseWrapper.getErrors().add("Lancamento não encontrado para o id " + id);
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		responseWrapper.setData(this.converterEntityLancamentoEmLancamentoDto(lancamento.get()));
		return ResponseEntity.ok(responseWrapper);
		
	}
	
	@PostMapping
	public ResponseEntity<ResponseWrapper<LancamentoDto>> adicionarNovoLancamento(@Valid @RequestBody LancamentoDto lancamentoDto,
			BindingResult result) throws ParseException{
		
		log.info("Adicionando lançamento: {}", lancamentoDto.toString());
		ResponseWrapper<LancamentoDto> responseWrapper = new ResponseWrapper<LancamentoDto>();
		
		verificaSeUsuarioExiste(lancamentoDto, result);
		
		Lancamento lancamento = this.converterLancamentoDtoParaEntityLancamento(lancamentoDto, result);
		
		if(result.hasErrors()) {
			log.info("Erro validando lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> responseWrapper.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		lancamento = this.lancamentoService.persistir(lancamento); //verificar sem receber em variável
		responseWrapper.setData(this.converterEntityLancamentoEmLancamentoDto(lancamento));
		return ResponseEntity.ok(responseWrapper);
		
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<ResponseWrapper<LancamentoDto>> atualizarLancamentoExistente(@PathVariable("id") Long id,
			@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result) throws ParseException{
		
		log.info("Atualizando Lançamento: {}", lancamentoDto.toString());
		
		ResponseWrapper<LancamentoDto> responseWrapper = new ResponseWrapper<LancamentoDto>();
		
		verificaSeUsuarioExiste(lancamentoDto, result);
		
		lancamentoDto.setId(Optional.of(id));
		
		Lancamento lancamento = this.converterLancamentoDtoParaEntityLancamento(lancamentoDto, result);
		
		if(result.hasErrors()) {
			log.error("Erro vlidando lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> responseWrapper.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		lancamento = this.lancamentoService.persistir(lancamento);
		responseWrapper.setData(this.converterEntityLancamentoEmLancamentoDto(lancamento));
		return ResponseEntity.ok(responseWrapper);
		
	}
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<ResponseWrapper<String>> removerLancamento(@PathVariable("id") Long id){
		log.info("Removendo lançamento: {}", id);
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<String>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if(!lancamento.isPresent()) {
			log.info("Erro ao remover devivo ao lançamento de ID {} ser inválido", id);
			responseWrapper.getErrors().add("Erro ao remover lançamento. Registro não encontrado para o id " + id);
			return ResponseEntity.badRequest().body(responseWrapper);
		}
		
		this.lancamentoService.remover(id);
		return ResponseEntity.ok(new ResponseWrapper<String>());
	}
	
	
	
	//--------------------------- Métodos de apoio -----------------------------------
	
	/**
	 * Valida um funcionário, verificando se ele é existente e válido no sistema
	 * 
	 * @param lancamentoDto
	 * @param result
	 */
	private void verificaSeUsuarioExiste(LancamentoDto lancamentoDto, BindingResult result) {
		
		if(lancamentoDto.getFuncionarioId() == null) {
			result.addError(new ObjectError("funcionario", "Funcionário ID não informado"));
			return;
		}
		
		log.info("Validando funcionário id {}", lancamentoDto.getFuncionarioId());
		
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(lancamentoDto.getFuncionarioId());
		if(!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não encontrado. ID inexistente."));
		}
		
	}
	
	private LancamentoDto converterEntityLancamentoEmLancamentoDto(Lancamento lancamento) {
		
		LancamentoDto lancamentoDto = new LancamentoDto();
		lancamentoDto.setId(Optional.of(lancamento.getId()));
		lancamentoDto.setData(this.dateFormat.format(lancamento.getData()));
		lancamentoDto.setTipo(lancamento.getTipo().toString());
		lancamentoDto.setDescricao(lancamento.getDescricao());
		lancamentoDto.setLocalizacao(lancamento.getLocalizacao());
		lancamentoDto.setFuncionarioId(lancamento.getFuncionario().getId());
		
		return lancamentoDto;
	}
	
	
	/**
	 * Converte um LancamentoDto para uma entidade Lancamento.
	 * (utilizado tanto para adição como atualização de um lançamento)
	 * 
	 * @param lancamentoDto
	 * @param result
	 * @return Lancamento
	 * @throws ParseException 
	 */
	private Lancamento converterLancamentoDtoParaEntityLancamento(LancamentoDto lancamentoDto, BindingResult result) throws ParseException {
		Lancamento lancamento = new Lancamento();

		if (lancamentoDto.getId().isPresent()) { //verifica se o lançamento já existe
			Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDto.getId().get());
			if (lanc.isPresent()) {
				lancamento = lanc.get();
			} else {
				result.addError(new ObjectError("lancamento", "Lançamento não encontrado."));
			}
		} else {
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(lancamentoDto.getFuncionarioId());
		}

		lancamento.setDescricao(lancamentoDto.getDescricao());
		lancamento.setLocalizacao(lancamentoDto.getLocalizacao());
		lancamento.setData(this.dateFormat.parse(lancamentoDto.getData()));

		//Verifica se o Enum que veio no DTO existe na Enum do Java
		
		if (EnumUtils.isValidEnum(TipoEnum.class, lancamentoDto.getTipo())) {
			lancamento.setTipo(TipoEnum.valueOf(lancamentoDto.getTipo()));
		} else {
			result.addError(new ObjectError("tipo", "Tipo inválido."));
		}

		return lancamento;
	}

}
