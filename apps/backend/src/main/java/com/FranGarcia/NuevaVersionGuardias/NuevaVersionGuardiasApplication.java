package com.FranGarcia.NuevaVersionGuardias;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(exclude = {
	org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
	org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class NuevaVersionGuardiasApplication {

	static {
		try {
			// Cargar variables de entorno desde el archivo .env en la raíz del proyecto
			Dotenv dotenv = Dotenv.configure()
					.directory(System.getProperty("user.dir") + "/../..") // Desde apps/backend ir a raíz
					.ignoreIfMissing() // No fallar si no existe el archivo
					.load();
			
			// Establecer las variables como propiedades del sistema para Spring Boot
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
				System.out.println("Loaded env variable: " + entry.getKey() + "=" + entry.getValue().substring(0, Math.min(10, entry.getValue().length())) + "...");
			});
			
			System.out.println("✅ Variables de entorno cargadas desde .env");
		} catch (Exception e) {
			System.out.println("⚠️ No se pudo cargar el archivo .env: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(NuevaVersionGuardiasApplication.class, args);
	}

}
