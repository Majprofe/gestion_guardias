package es.iesjandula.guardias;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
public class GuardiasApplication {

	static {
		try {
			// Cargar variables de entorno desde el archivo .env en la raíz del proyecto
			String projectRoot = System.getProperty("user.dir");
			
			// Si estamos en apps/backend, subir dos niveles
			if (projectRoot.endsWith("apps\\backend") || projectRoot.endsWith("apps/backend")) {
				projectRoot = projectRoot + "/../..";
			}
			// Si estamos en la raíz del proyecto, usar directamente
			else if (!projectRoot.contains("apps")) {
				projectRoot = projectRoot;
			}
			// Si estamos en otra ubicación, intentar encontrar el .env
			else {
				projectRoot = projectRoot + "/../..";
			}
			
			Dotenv dotenv = Dotenv.configure()
					.directory(projectRoot)
					.ignoreIfMissing()
					.load();
			
			// Establecer las variables como propiedades del sistema para Spring Boot
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
				System.out.println("Loaded env variable: " + entry.getKey());
			});
			
			System.out.println("✅ Variables de entorno cargadas desde .env en: " + projectRoot);
		} catch (Exception e) {
			System.out.println("⚠️ No se pudo cargar el archivo .env: " + e.getMessage());
			System.out.println("⚠️ Working directory: " + System.getProperty("user.dir"));
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(GuardiasApplication.class, args);
	}

}
