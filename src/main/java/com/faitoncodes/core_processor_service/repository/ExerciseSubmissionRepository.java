package com.faitoncodes.core_processor_service.repository;

import com.faitoncodes.core_processor_service.dao.AgrupamentoUserClass;
import com.faitoncodes.core_processor_service.dao.ExerciseSubmission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseSubmissionRepository extends CrudRepository<ExerciseSubmission, Long> {
    Optional<ExerciseSubmission> findByStudentIdAndExerciseId(Long studentId, Long exerciseId);

    List<ExerciseSubmission> findByExerciseId(Long exerciseId);
}
