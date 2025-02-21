package com.banquito.paymentprocessor.validafraude.banquito.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;

import java.util.List;

@Repository
public interface ReglaFraudeRepository extends CrudRepository<ReglaFraude, String> {
    
    List<ReglaFraude> findByEstadoTrue();
    
    List<ReglaFraude> findByTipoReglaAndEstadoTrue(String tipoRegla);
} 