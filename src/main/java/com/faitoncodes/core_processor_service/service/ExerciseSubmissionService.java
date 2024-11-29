package com.faitoncodes.core_processor_service.service;

import com.faitoncodes.core_processor_service.dao.Exercise;
import com.faitoncodes.core_processor_service.dao.ExerciseSubmission;
import com.faitoncodes.core_processor_service.dto.StatusSubmissionDTO;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionDTO;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionExternalRequest;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionIndividualResponseDTO;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseSubmissionResponseDTO;
import com.faitoncodes.core_processor_service.repository.ExerciseRepository;
import com.faitoncodes.core_processor_service.repository.ExerciseSubmissionRepository;
import com.faitoncodes.core_processor_service.repository.TestCaseRepository;
import com.faitoncodes.core_processor_service.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Log4j2
public class ExerciseSubmissionService {

    @Autowired
    ExerciseSubmissionRepository exerciseSubmissionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestCaseRepository testCaseRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Value("${uri.codeEvaluatorService}")
    String codeEvaluatorServiceUri;

    public ExerciseSubmission submitCode(ExerciseSubmissionDTO exerciseSubmissionRequest) throws InterruptedException {

        Optional<Exercise> optionalExercise = exerciseRepository.findById(exerciseSubmissionRequest.getExerciseId());

        ZonedDateTime actualDateTime = LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"));

        if(optionalExercise.isPresent()){
            if(optionalExercise.get().getDueDate().isBefore(actualDateTime.toLocalDateTime())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submissao passou do prazo de entrega do exercicio.");
            }
        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercicio nao encontrado.");
        }


        Optional<ExerciseSubmission> OptionalExistedSubmission = exerciseSubmissionRepository.findByStudentIdAndExerciseId(exerciseSubmissionRequest.getStudentId(), exerciseSubmissionRequest.getExerciseId());

        if(OptionalExistedSubmission.isPresent()){
            ExerciseSubmission existedSubmission = OptionalExistedSubmission.get();
            existedSubmission.setStudent_code(exerciseSubmissionRequest.getCodeSent());
            existedSubmission.setExercise_status(0);
            existedSubmission.setSubmission_date(LocalDateTime.now());

            exerciseSubmissionRepository.save(existedSubmission);

            return existedSubmission;
        }


        ExerciseSubmission newExerciseSubmission = ExerciseSubmission.builder()
                .exercise_status(0)
                .exerciseId(exerciseSubmissionRequest.getExerciseId())
                .studentId(exerciseSubmissionRequest.getStudentId())
                .student_code(exerciseSubmissionRequest.getCodeSent())
                .submission_date(LocalDateTime.now())
                .build();

        exerciseSubmissionRepository.save(newExerciseSubmission);

        return newExerciseSubmission;
    }

    @Async
    public void processStudentCode(ExerciseSubmission exerciseSubmissionRequest) throws InterruptedException {

        ExerciseSubmissionExternalRequest request = ExerciseSubmissionExternalRequest.builder()
                .CodigoFonte(exerciseSubmissionRequest.getStudent_code())
                .idAluno(exerciseSubmissionRequest.getStudentId())
                .idExercicio(exerciseSubmissionRequest.getExerciseId())
                .idSubmissao(exerciseSubmissionRequest.getSubmission_id())
                .build();

        log.info("Enviando pedido para correção de código");
        RestClient restClient = RestClient.create();
        ResponseEntity<Void> response = restClient.post()
                .uri(codeEvaluatorServiceUri + "AnalisarCodigo/ValidarCasosDeTest")
                .contentType(APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Pedido enviado!");
    }

    public List<ExerciseSubmissionResponseDTO> getSubmissionFromExercise(Long exerciseId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<ExerciseSubmission> exerciseSubmissionsList = exerciseSubmissionRepository.findByExerciseId(exerciseId);

        List<ExerciseSubmissionResponseDTO> responseList = new ArrayList<>();

        Integer totalTestCasesExercise = testCaseRepository.findByExerciseId(exerciseId).size();


        exerciseSubmissionsList.forEach(exerciseSubmission -> {

            ExerciseSubmissionResponseDTO exerciseSubmissionResponseDTO = ExerciseSubmissionResponseDTO.builder()
                    .exerciseId(exerciseId)
                    .correctTestCases(exerciseSubmission.getCorrect_test_cases())
                    .studentName(userRepository.getUserName(exerciseSubmission.getStudentId()))
                    .submittedTime(exerciseSubmission.getSubmission_date().format(formatter))
                    .totalTestCases(totalTestCasesExercise)
                    .build();

            responseList.add(exerciseSubmissionResponseDTO);
        });

        return responseList;
    }

    public ExerciseSubmissionIndividualResponseDTO getSubmissionFromUser(Long exerciseId, Long userIdToken) {
        Optional<ExerciseSubmission> optionalExerciseSubmission = exerciseSubmissionRepository.findByStudentIdAndExerciseId(userIdToken, exerciseId);

        Integer totalTestCasesExercise = testCaseRepository.findByExerciseId(exerciseId).size();

        if (optionalExerciseSubmission.isEmpty()){
            return ExerciseSubmissionIndividualResponseDTO.builder()
                    .exerciseId(exerciseId)
                    .correctTestCases(0)
                    .totalTestCases(totalTestCasesExercise)
                    .status("Código ainda não submetido")
                    .statusColor("0,0,0,0.5")
                    .build();
        }

        ExerciseSubmission foundExerciseSubmission = optionalExerciseSubmission.get();

        StatusSubmissionDTO statusCodeSubmission = statusCodeToStringFormat(foundExerciseSubmission.getExercise_status());

        return ExerciseSubmissionIndividualResponseDTO.builder()
                .exerciseId(exerciseId)
                .status(statusCodeSubmission.getStatusName())
                .statusColor(statusCodeSubmission.getStatusColor())
                .totalTestCases(totalTestCasesExercise)
                .correctTestCases(foundExerciseSubmission.getCorrect_test_cases())
                .build();
    }

    private StatusSubmissionDTO statusCodeToStringFormat(Integer statusCode){

        if(statusCode == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível encontrar status da submissão");
        }

        if(statusCode == 0){
            return StatusSubmissionDTO.builder()
                    .statusName("Código submetido mas ainda não foi analisado.")
                    .statusColor("251, 153, 94, 1")
                    .build();

        }
        if(statusCode == 1){
            return StatusSubmissionDTO.builder()
                    .statusName("Código submetido e analisado com sucesso!")
                    .statusColor("4, 117, 23,1")
                    .build();
        }
        if(statusCode == 2){
            return StatusSubmissionDTO.builder()
                    .statusName("Código submetido porém compilação falhou!")
                    .statusColor("255,0,0,1")
                    .build();
        }

        return null;
    }
}
