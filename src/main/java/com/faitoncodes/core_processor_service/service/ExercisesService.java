package com.faitoncodes.core_processor_service.service;

import com.faitoncodes.core_processor_service.dao.AgrupamentoUserClass;
import com.faitoncodes.core_processor_service.dao.Exercise;
import com.faitoncodes.core_processor_service.dao.TestCase;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseDTO;
import com.faitoncodes.core_processor_service.dto.exercises.ExerciseInfoDTO;
import com.faitoncodes.core_processor_service.repository.*;
import com.faitoncodes.core_processor_service.util.DateFormatterUtil;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Log4j2
public class ExercisesService {
   @Autowired
   ExerciseRepository exerciseRepository;

   @Autowired
   ClassRepository classRepository;

   DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

   @Autowired
   TestCaseRepository testCaseRepository;

   @Autowired
   AgrupamentoUserClassRepository agrupamentoUserClassRepository;

   @Autowired
   UserRepository userRepository;

   PropertyMap<ExerciseDTO, Exercise> customMappingExerciseDTO = new PropertyMap<ExerciseDTO, Exercise>() {
      protected void configure() {
         skip(destination.getId());
      }
   };

   public Exercise createExercise(ExerciseDTO exerciseDTO) {
      if(!classRepository.existsById(exerciseDTO.getClassId())){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da classe não existe para criar o exercício");
      }
      if(!classRepository.existsByTeacherId(exerciseDTO.getTeacherId())){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de professor informado não é dono da turma.");
      }

      LocalDateTime dueTimeExercise = LocalDateTime.parse(exerciseDTO.getDueDate(), dataFormatter);

      Exercise newExercise = Exercise.builder()
              .title(exerciseDTO.getTitle())
              .description(exerciseDTO.getDescription() != null? exerciseDTO.getDescription() : null)
              .dueDate(dueTimeExercise)
              .creationDate(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime())
              .classId(exerciseDTO.getClassId())
              .build();

      try{
         newExercise = exerciseRepository.save(newExercise);
         List<TestCase> extractedTestCases = extractListOfTestCases(exerciseDTO.getTestCases(), newExercise.getId());

         if(!extractedTestCases.isEmpty()){
            extractedTestCases.forEach(testCase -> {
               testCaseRepository.save(testCase);
            });
         }

         return newExercise;
      }catch (Exception e){
         log.error(e);
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar novo exercicio.");
      }


   }

   public List<ExerciseInfoDTO> getExercisesFromClass(Long classId) {
      List<ExerciseInfoDTO> exerciseInfoDTOList = new ArrayList<>();
      List<Exercise> exercisesList = exerciseRepository.findByClassId(classId);

      exercisesList.forEach(exercise -> {
         if(exercise.getDeletionDate() == null){

            exerciseInfoDTOList.add(ExerciseInfoDTO.builder()
                    .id(exercise.getId())
                    .title(exercise.getTitle())
                    .description(exercise.getDescription())
                    .testCases(getTestCasesFromExercise(exercise.getId()))
                    .dueDate(DateFormatterUtil.extractDateFormDueDate(exercise.getDueDate()))
                    .updatedDate(exercise.getUpdatedDate() != null? DateFormatterUtil.extractDateFormUpdatedDateAllExercises(exercise.getUpdatedDate()) : null)
                    .classId(classId)
                    .teacherName(userRepository.getUserName(classRepository.findTeacherIdByClassId(classId)))
                    .build()
            );
         }
      });

      return exerciseInfoDTOList;
   }

   private List<TestCase> extractListOfTestCases(String testCasesString, Long exerciseId) {
      if(testCasesString.isBlank()){
         return List.of();
      }

      List<String> testCaseStringList = Arrays.asList(testCasesString.split("/"));
      List<TestCase> listTestCases = new ArrayList<>();
      testCaseStringList.forEach(testCase -> {
         String[] inputAndOutput = testCase.split(";");
         TestCase newTestCase = TestCase.builder()
                 .exerciseId(exerciseId)
                 .input(StringUtils.deleteWhitespace(inputAndOutput[0]))
                 .expectedOutput(StringUtils.deleteWhitespace(inputAndOutput[1]))
                 .build();
         listTestCases.add(newTestCase);
      });

      return listTestCases;

   }

   @Transactional
   public Exercise updateExercise(ExerciseDTO exerciseDTO, Long exerciseId) {
      if(exerciseId == null){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do exercício não informado");
      }

      Optional<Exercise> actualExercise = exerciseRepository.findById(exerciseId);

      if(actualExercise.isEmpty()){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercício não encontrado.");
      }
      if(!classRepository.existsById(exerciseDTO.getClassId())){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da classe não existe para criar o exercício");
      }
      if(!classRepository.existsByTeacherId(exerciseDTO.getTeacherId())){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de professor informado não é dono da turma.");
      }

      ModelMapper modelMapper = new ModelMapper();
      Exercise updatedExercise = actualExercise.get();
      updatedExercise.setUpdatedDate(LocalDateTime.now());
      if(exerciseDTO.getDueDate() != null){
         LocalDateTime dueTimeExercise = LocalDateTime.parse(exerciseDTO.getDueDate(), dataFormatter);
         updatedExercise.setDueDate(dueTimeExercise);
      }

      updatedExercise.setUpdatedDate(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime());

      modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
      modelMapper.addMappings(customMappingExerciseDTO);
      modelMapper.map(exerciseDTO, updatedExercise);

      try{
         updatedExercise = exerciseRepository.save(updatedExercise);


         if(testCaseRepository.existsByExerciseId(exerciseId)){
            testCaseRepository.deleteByExerciseId(exerciseId);
         }

         List<TestCase> extractedTestCases = extractListOfTestCases(exerciseDTO.getTestCases() != null? exerciseDTO.getTestCases() : "", updatedExercise.getId());

         if(!extractedTestCases.isEmpty()){
            extractedTestCases.forEach(testCase -> {
               testCaseRepository.save(testCase);
            });
         }


         return updatedExercise;
      }catch (Exception e){
         log.error(e);
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar novo exercicio.");
      }

   }

   private String getTestCasesFromExercise(Long exerciseId) {
      List<TestCase> listTestCaseEntity = testCaseRepository.findByExerciseId(exerciseId);
      StringBuilder stringFormatted = new StringBuilder();
      listTestCaseEntity.forEach(testCase -> {
         stringFormatted.append(testCase.getInput());
         stringFormatted.append(";");
         stringFormatted.append(testCase.getExpectedOutput());
         stringFormatted.append("/");
      });

      return stringFormatted.toString();
   }

   public ExerciseInfoDTO getExerciseById(Long exerciseId, Long userIdFromToken) {

      Optional<Exercise> optionalExercise = exerciseRepository.findById(exerciseId);

      if(optionalExercise.isPresent()){
         Exercise exerciseDao = optionalExercise.get();
         if(exerciseDao.getDeletionDate() == null){
            validateIfUserIsAllowedToAccessExercise(exerciseDao, userIdFromToken);

            return ExerciseInfoDTO.builder()
                    .id(exerciseDao.getId())
                    .title(exerciseDao.getTitle())
                    .description(exerciseDao.getDescription())
                    .testCases(getTestCasesFromExercise(exerciseDao.getId()))
                    .dueDate(DateFormatterUtil.extractDateFormDueDate(exerciseDao.getDueDate()))
                    .dueDateModal(DateFormatterUtil.extractDateModalDueDate(exerciseDao.getDueDate()))
                    .updatedDate(exerciseDao.getUpdatedDate() != null? DateFormatterUtil.extractDateFormUpdatedDateForGetExerciseId(exerciseDao.getUpdatedDate()) : null)
                    .classId(exerciseDao.getClassId())
                    .teacherName(userRepository.getUserName(classRepository.findTeacherIdByClassId(exerciseDao.getClassId())))
                    .build();
         }
      }else{
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercício não existe ou foi deletado.");
      }
      return null;
   }

   private void validateIfUserIsAllowedToAccessExercise(Exercise exercise, Long userIdFromToken) {

      List<AgrupamentoUserClass> agrupamentoUserClassList = agrupamentoUserClassRepository.findByUserId(userIdFromToken);

      boolean exerciseFound = agrupamentoUserClassList.stream().anyMatch(agrupamento -> agrupamento.getClassId().equals(exercise.getClassId()));

      if(!exerciseFound){
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno não tem acesso a esse exercício.");
      }

   }
}
