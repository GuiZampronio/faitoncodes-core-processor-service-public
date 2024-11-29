package com.faitoncodes.core_processor_service.dto.exercises;

import lombok.Builder;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@Builder
public class ExerciseSubmissionResponseDTO {
    private Long exerciseId;
    private String studentName;
    private Integer totalTestCases;
    private Integer correctTestCases;
    private String submittedTime;

}
