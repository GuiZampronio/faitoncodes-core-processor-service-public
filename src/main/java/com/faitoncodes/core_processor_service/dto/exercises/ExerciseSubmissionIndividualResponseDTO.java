package com.faitoncodes.core_processor_service.dto.exercises;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseSubmissionIndividualResponseDTO {
    private Long exerciseId;
    private Integer totalTestCases;
    private Integer correctTestCases;
    private String status;
    private String statusColor;

}
