package com.faitoncodes.core_processor_service.dto.classes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassStudentsDTO {
    private String studentName;

    private String studentEmail;

    private String studentFirstLetter;

    private String studentColor;
}
