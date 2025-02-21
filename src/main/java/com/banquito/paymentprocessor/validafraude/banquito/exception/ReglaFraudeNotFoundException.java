package com.banquito.paymentprocessor.validafraude.banquito.exception;

public class ReglaFraudeNotFoundException extends RuntimeException {
    
    private final String codReglaFraude;

    public ReglaFraudeNotFoundException(String codReglaFraude) {
        super();
        this.codReglaFraude = codReglaFraude;
    }

    @Override
    public String getMessage() {
        return "No se encontró la regla de fraude con código: " + codReglaFraude;
    }
} 