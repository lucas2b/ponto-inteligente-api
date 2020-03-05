package com.lucas.pontointeligente.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {
	
	private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);
	
	public PasswordUtils() {
		
	}
	
	/**
	 * Gera um hash utilizando o BCrypt.
	 * 
	 * @param senha
	 * @return string
	 * 
	 */
	public static String gerarBcrypt(String senha) {
		
		if(senha == null || senha.equals("")) {
			return senha;
		}else {
			log.info("gerando senha...");
			BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();
			return bCryptEncoder.encode(senha);
		}
			
	}

}
