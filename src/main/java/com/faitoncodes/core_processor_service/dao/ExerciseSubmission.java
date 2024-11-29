package com.faitoncodes.core_processor_service.dao;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "exercise_submission")
@Table(name = "exercise_submission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submission_id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "exercise_status", nullable = false)
    private Integer exercise_status;

    @Column(name = "student_code", nullable = false)
    private String student_code;

    @Column(name = "correct_test_cases", nullable = true)
    private Integer correct_test_cases;

    @Column(name = "submission_date", updatable = true, nullable = false)
    private LocalDateTime submission_date;

}
