package com.banquito.paymentprocessor.validafraude.banquito;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
	DataSourceAutoConfiguration.class,
	DataSourceTransactionManagerAutoConfiguration.class,
	HibernateJpaAutoConfiguration.class,
	JpaRepositoriesAutoConfiguration.class
})
public class BanquitoApplication {

	private static final Logger log = LoggerFactory.getLogger(BanquitoApplication.class);

	@Value("${microservicio.mdc.nombre}")
	private String nombreMicroservicio;

	@Value("${microservicio.mdc.version}")
	private String versionMicroservicio;

	public static void main(String[] args) {
		SpringApplication.run(BanquitoApplication.class, args);
	}

	@Bean
	public CommandLineRunner configureMDCOnStartup() {
		return args -> {
			try {
				MDC.put("microservicio", nombreMicroservicio);
				MDC.put("version", versionMicroservicio);
				
				log.info("Iniciando microservicio de validación de fraude con Redis...");
				log.info("Configuración MDC: microservicio={}, version={}", 
						nombreMicroservicio, versionMicroservicio);
			} finally {
				MDC.clear();
			}
		};
	}

}
