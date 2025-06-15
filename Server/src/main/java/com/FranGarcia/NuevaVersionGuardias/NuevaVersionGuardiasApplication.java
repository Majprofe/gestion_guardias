package com.FranGarcia.NuevaVersionGuardias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class NuevaVersionGuardiasApplication {

	public static void main(String[] args) {
		SpringApplication.run(NuevaVersionGuardiasApplication.class, args);
	}

}
