package com.banquito.paymentprocessor.validafraude.banquito.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReglaFraudeMapper {
    
    ReglaFraudeDTO toDTO(ReglaFraude model);
    
    ReglaFraude toModel(ReglaFraudeDTO dto);
} 