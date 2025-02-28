package com.banquito.paymentprocessor.validafraude.banquito.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Configuration
@Slf4j
public class MDCConfig {

    @Value("${microservicio.mdc.nombre}")
    private String nombreMicroservicio;

    @Value("${microservicio.mdc.version}")
    private String versionMicroservicio;

    @Bean
    public FilterRegistrationBean<MDCFilter> mdcFilterRegistration() {
        log.info("Registrando filtro MDC para el microservicio: {}", nombreMicroservicio);
        FilterRegistrationBean<MDCFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MDCFilter(nombreMicroservicio, versionMicroservicio));
        registration.addUrlPatterns("/*");
        registration.setName("mdcFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    public static class MDCFilter implements Filter {

        private final String nombreMicroservicio;
        private final String versionMicroservicio;

        public MDCFilter(String nombreMicroservicio, String versionMicroservicio) {
            this.nombreMicroservicio = nombreMicroservicio;
            this.versionMicroservicio = versionMicroservicio;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            try {
                MDC.put("microservicio", nombreMicroservicio);
                MDC.put("version", versionMicroservicio);
                
                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;
                    MDC.put("requestId", httpRequest.getHeader("X-Request-ID"));
                    MDC.put("path", httpRequest.getRequestURI());
                    MDC.put("method", httpRequest.getMethod());
                }
                
                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        }
    }
} 