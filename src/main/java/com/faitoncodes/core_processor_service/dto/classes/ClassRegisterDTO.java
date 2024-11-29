package com.faitoncodes.core_processor_service.dto.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ClassRegisterDTO {
    private String className;

    private String announcement;

    @JsonIgnore
    private Long teacherId;
}
