package com.lucas.pontointeligente.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.lucas.pontointeligente.api.entities.Empresa;
import com.lucas.pontointeligente.api.entities.Funcionario;
import com.lucas.pontointeligente.api.enums.PerfilEnum;
import com.lucas.pontointeligente.api.repositories.EmpresaRepository;
import com.lucas.pontointeligente.api.repositories.FuncionarioRepository;
import com.lucas.pontointeligente.api.utils.PasswordUtils;

@SpringBootTest
@ActiveProfiles("test")
public class FuncionarioRepositoryTest {
	
	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	private static final String EMAIL = "email@email.com";
	private static final String CPF = "24291173474";
	
	@BeforeEach
	public void setUp() throws Exception{
		Empresa empresa = this.empresaRepository.save(obterDadosEmpresa());
		this.funcionarioRepository.save(obterDadosFuncionario(empresa));
		
		
	}
	
	@AfterEach
	public void tearDown() {
		this.empresaRepository.deleteAll();
	}
	
	
	private Empresa obterDadosEmpresa() {
		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Empresa De Exemplo");
		empresa.setCnpj("5146364500010");
		return empresa;
	}
	
	
	private Funcionario obterDadosFuncionario(Empresa empresa) throws NoSuchAlgorithmException{
		Funcionario funcionario = new Funcionario();
		funcionario.setNome("Fulano de tal");
		funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.gerarBcrypt("123456"));
		funcionario.setCpf(CPF);
		funcionario.setEmail(EMAIL);
		funcionario.setEmpresa(empresa);
		return funcionario;
	}
	
	@Test
	public void testBuscarFuncionarioPorEmail() {
		Funcionario funcionario = this.funcionarioRepository.findByEmail(EMAIL);
		
		assertEquals(EMAIL, funcionario.getEmail());
	}
	
	@Test
	public void testBuscarFuncionarioPorCpf() {
		Funcionario funcionario = this.funcionarioRepository.findByCpf(CPF);
		
		assertEquals(CPF, funcionario.getCpf());
	}
	
	@Test
	public void testBuscarFuncionarioPorEmailOuCpf() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail(CPF, EMAIL);
		
		assertNotNull(funcionario);
	}
	
	
	@Test
	public void testBuscarFuncionarioPorEmailOuPorCpfParaEmailInvalido() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail(CPF, "email@invalido.com");
		
		assertNotNull(funcionario);
	}
	
	@Test
	public void testBuscarFuncionarioPorEmailOuPorCpfParaCpfInvalido() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail("123", EMAIL);
		
		assertNotNull(funcionario);
	}
	
	

}
