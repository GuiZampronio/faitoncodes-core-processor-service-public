package com.faitoncodes.core_processor_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusSubmissionDTO {
    private String statusName;

    private String statusColor;
}
