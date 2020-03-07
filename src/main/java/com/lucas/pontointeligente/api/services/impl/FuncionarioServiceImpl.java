package com.lucas.pontointeligente.api.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lucas.pontointeligente.api.entities.Funcionario;
import com.lucas.pontointeligente.api.repositories.FuncionarioRepository;
import com.lucas.pontointeligente.api.services.FuncionarioService;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {
	
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioServiceImpl.class);
	
	@Autowired
	private FuncionarioRepository funcionarioRepository;

	@Override
	public Funcionario persistir(Funcionario funcionario) {
		log.info("Persistindo funcionario {}", funcionario);
		return this.funcionarioRepository.save(funcionario);
	}

	@Override
	public Optional<Funcionario> buscarPorCpf(String cpf) {
		
		log.info("Buscando funcionario pelo cpf {}", cpf);
		
		Funcionario funcionario = this.funcionarioRepository.findByCpf(cpf);
		return Optional.ofNullable(funcionario);
	}

	@Override
	public Optional<Funcionario> buscarPorEmail(String email) {
		
		log.info("Buscando funcionario pelo email {}", email);
		
		Funcionario funcionario = this.funcionarioRepository.findByEmail(email);
		return Optional.ofNullable(funcionario);
	}

	@Override
	public Optional<Funcionario> buscarPorId(Long id) {
		
		log.info("Buscando funcionario pelo id {}", id);
		
		Optional<Funcionario> funcionario = this.funcionarioRepository.findById(id);
		return funcionario;
	}

}
