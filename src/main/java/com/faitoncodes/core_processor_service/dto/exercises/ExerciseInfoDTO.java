package com.faitoncodes.core_processor_service.dto.exercises;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExerciseInfoDTO {
    private Long id;

    private String title;

    private String description;

    private String testCases;

    private Long classId;

    private String dueDate;

    private String updatedDate;

    private String teacherName;

    private String dueDateModal;

}
