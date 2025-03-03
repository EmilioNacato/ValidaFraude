package com.banquito.paymentprocessor.validafraude.banquito.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración personalizada para Spring WebMVC.
 * Esta configuración asegura que las rutas API no entren en conflicto con recursos estáticos
 * y configura el soporte CORS.
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Configurando manejadores de recursos estáticos");
        
        // Configurar explícitamente los manejadores para Swagger UI
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.15.5/");
        
        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        // Recursos estáticos regulares
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        log.info("Manejadores de recursos estáticos configurados correctamente");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configurando mapeos CORS");
        
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        
        log.info("Mapeos CORS configurados correctamente");
    }
} 