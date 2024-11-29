package com.faitoncodes.core_processor_service.controller;

import com.faitoncodes.core_processor_service.dao.ExerciseSubmission;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionDTO;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionIndividualResponseDTO;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionResponseDTO;
import com.faitoncodes.core_processor_service.service.ExerciseSubmissionService;
import com.faitoncodes.core_processor_service.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exerciseSubmission")
@Log4j2
@SecurityRequirement(name = "bearerAuth")
public class ExerciseSubmissionController {
    @Autowired
    ExerciseSubmissionService exerciseSubmissionService;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping("/submitCode")
    public ResponseEntity<ExerciseSubmission> submitCodeToExercise(@Valid @RequestBody ExerciseSubmissionDTO exerciseSubmissionRequest) throws InterruptedException {
        exerciseSubmissionRequest.setStudentId(jwtTokenUtil.getIdFromToken());
        ExerciseSubmission exerciseSubmitted = exerciseSubmissionService.submitCode(exerciseSubmissionRequest);

        //Async method
        exerciseSubmissionService.processStudentCode(exerciseSubmitted);

        return ResponseEntity.ok(exerciseSubmitted);
    }

    @GetMapping("/retrieveSubmissions")
    public ResponseEntity<List<ExerciseSubmissionResponseDTO>> getExerciseSubmissions(@RequestParam Long exerciseId){
        List<ExerciseSubmissionResponseDTO> exerciseSubmissionsList = exerciseSubmissionService.getSubmissionFromExercise(exerciseId);
        return ResponseEntity.ok(exerciseSubmissionsList);
    }

    @GetMapping("/retrieveSubmission")
    public ResponseEntity<ExerciseSubmissionIndividualResponseDTO> getExerciseSubmissionByID(@RequestParam Long exerciseId){
        Long userIdToken = jwtTokenUtil.getIdFromToken();
        ExerciseSubmissionIndividualResponseDTO exerciseSubmissionResponse = exerciseSubmissionService.getSubmissionFromUser(exerciseId, userIdToken);
        return ResponseEntity.ok(exerciseSubmissionResponse);
    }

}
