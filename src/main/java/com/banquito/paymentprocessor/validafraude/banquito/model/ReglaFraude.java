package com.banquito.paymentprocessor.validafraude.banquito.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@RedisHash("regla_fraude")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ReglaFraude implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String codReglaFraude;

    private String nombreRegla;

    private String descripcion;

    @Indexed
    private String tipoRegla;

    private BigDecimal montoLimite;

    private Integer maxTransacciones;

    private Integer periodoMinutos;

    @Indexed
    private Boolean estado;

    public ReglaFraude(String codReglaFraude) {
        this.codReglaFraude = codReglaFraude;
    }

    @Override
    public int hashCode() {
        return codReglaFraude != null ? codReglaFraude.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ReglaFraude other = (ReglaFraude) obj;
        return codReglaFraude != null && codReglaFraude.equals(other.codReglaFraude);
    }
} 