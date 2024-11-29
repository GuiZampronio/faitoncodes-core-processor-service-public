package com.faitoncodes.core_processor_service.repository;

import com.faitoncodes.core_processor_service.dao.Class;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassRepository extends CrudRepository<Class, Long> {
    boolean existsByClassCode(String classCode);

    boolean existsByTeacherId(Long teacherId);

    Optional<Class> findByClassCode(String classCode);

    @Query(value = "SELECT c.teacher_id FROM public.class c WHERE c.class_id = :id", nativeQuery = true)
    Long findTeacherIdByClassId(@Param("id")Long classId);
}
