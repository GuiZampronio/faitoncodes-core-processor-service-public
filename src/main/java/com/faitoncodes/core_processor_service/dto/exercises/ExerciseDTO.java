package com.faitoncodes.core_processor_service.dto.exercises;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseDTO {

    @NotNull
    private Long classId;
    @NotNull
    private String title;
    private String description;
    private String testCases;
    @NotNull
    private String dueDate;
    @JsonIgnore
    private Long teacherId;
}
