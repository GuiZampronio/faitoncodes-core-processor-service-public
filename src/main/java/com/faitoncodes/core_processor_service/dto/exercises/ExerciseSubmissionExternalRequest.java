package com.faitoncodes.core_processor_service.dto.exercises;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseSubmissionExternalRequest {
    private String CodigoFonte;
    private Long idExercicio;
    private Long idAluno;
    private Long idSubmissao;
}