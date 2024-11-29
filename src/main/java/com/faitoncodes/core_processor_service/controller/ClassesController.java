package com.faitoncodes.core_processor_service.controller;

import com.faitoncodes.core_processor_service.dao.AgrupamentoUserClass;
import com.faitoncodes.core_processor_service.dao.Class;
import com.faitoncodes.core_processor_service.dto.classes.ClassRegisterDTO;
import com.faitoncodes.core_processor_service.dto.classes.ClassStudentsDTO;
import com.faitoncodes.core_processor_service.dto.classes.ClassesInfoDTO;
import com.faitoncodes.core_processor_service.service.ClassesService;
import com.faitoncodes.core_processor_service.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/class")
@Log4j2
@SecurityRequirement(name = "bearerAuth")
public class ClassesController {
    @Autowired
    ClassesService classesService;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping("/createClass")
    public ResponseEntity<Class> createNewClass(@RequestBody ClassRegisterDTO classRegisterDTO){
        classRegisterDTO.setTeacherId(jwtTokenUtil.getIdFromToken());
        try{
            Class newClass = classesService.createClass(classRegisterDTO);
            return ResponseEntity.ok(newClass);
        }catch(Exception e){
            log.error(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao criar nova turma!", e);
        }
    }

    @PutMapping("/editClass")
    public ResponseEntity<Class> updateClass(@RequestParam Long classId, @RequestBody ClassRegisterDTO classRegisterDTO){
        try{
            Class updatedClass = classesService.updateClass(classId, classRegisterDTO, jwtTokenUtil.getIdFromToken());
            return ResponseEntity.ok(updatedClass);
        }catch (Exception e){
            log.error(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao atualizar turma.", e);
        }

    }

    @PostMapping("/linkClass")
    public ResponseEntity<AgrupamentoUserClass> linkUserToClass(@RequestParam String classCode){
        Long userId = jwtTokenUtil.getIdFromToken();
        try{
            AgrupamentoUserClass newAgrupamento = classesService.linkUserToClass(userId, classCode);
            return ResponseEntity.ok(newAgrupamento);
        }catch (Exception e){
            log.error(e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao entrar na classe.");
        }

    }

    @GetMapping("/getClasses")
    public ResponseEntity<List<ClassesInfoDTO>> getAllClassesFromUser(){
        Long userId = jwtTokenUtil.getIdFromToken();
        List<ClassesInfoDTO> classesInfos = classesService.getClassesFromUser(userId);
        return ResponseEntity.ok(classesInfos);
    }

    @GetMapping("/getClass")
    public ResponseEntity<ClassesInfoDTO> getClassById(@RequestParam Long classId){
        Long userId = jwtTokenUtil.getIdFromToken();
        ClassesInfoDTO classInfo = classesService.getClassById(userId, classId);
        return ResponseEntity.ok(classInfo);
    }

    @GetMapping("getStudentsFromClass")
    public ResponseEntity<List<ClassStudentsDTO>> getStudentsFromClass(@RequestParam Long classId){
        List<ClassStudentsDTO> classStudents = classesService.getStudentsFromClass(classId);
        return ResponseEntity.ok(classStudents);
    }

}
