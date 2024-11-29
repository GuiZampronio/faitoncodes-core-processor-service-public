package com.faitoncodes.core_processor_service.dto.exercises;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseSubmissionDTO {
    @NotNull
    private Long exerciseId;

    @JsonIgnore
    private Long studentId;
    @NotNull
    private String codeSent;
}
