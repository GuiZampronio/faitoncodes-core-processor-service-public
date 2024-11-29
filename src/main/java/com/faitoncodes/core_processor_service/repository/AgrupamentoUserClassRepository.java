package com.faitoncodes.core_processor_service.repository;

import com.faitoncodes.core_processor_service.dao.AgrupamentoUserClass;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgrupamentoUserClassRepository extends CrudRepository<AgrupamentoUserClass, Long> {
    List<AgrupamentoUserClass> findByUserId(Long userId);

    Optional<AgrupamentoUserClass> findByUserIdAndClassId(Long userId, Long classId);

    List<AgrupamentoUserClass> findByClassId(Long classId);
}
