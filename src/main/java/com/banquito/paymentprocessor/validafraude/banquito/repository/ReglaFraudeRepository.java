package com.banquito.paymentprocessor.validafraude.banquito.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;

@Repository
public interface ReglaFraudeRepository extends CrudRepository<ReglaFraude, String> {
    
    List<ReglaFraude> findByEstadoTrue();
    
    List<ReglaFraude> findByTipoReglaAndEstadoTrue(String tipoRegla);
} 