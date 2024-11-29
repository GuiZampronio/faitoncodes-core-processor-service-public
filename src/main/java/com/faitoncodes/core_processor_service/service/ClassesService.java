package com.faitoncodes.core_processor_service.service;

import com.faitoncodes.core_processor_service.dao.AgrupamentoUserClass;
import com.faitoncodes.core_processor_service.dao.Class;
import com.faitoncodes.core_processor_service.dto.classes.ClassRegisterDTO;
import com.faitoncodes.core_processor_service.dto.classes.ClassStudentsDTO;
import com.faitoncodes.core_processor_service.dto.classes.ClassesInfoDTO;
import com.faitoncodes.core_processor_service.dto.user.UsuarioDTO;
import com.faitoncodes.core_processor_service.repository.AgrupamentoUserClassRepository;
import com.faitoncodes.core_processor_service.repository.ClassRepository;
import com.faitoncodes.core_processor_service.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.util.*;
import java.util.List;

@Service
@Log4j2
public class ClassesService {

    private static final String MIX_STRING = "abcdefghijklmnopqrstuvwxyz1234567890";

    private static final int MIX_STRING_LENGTH = MIX_STRING.length();

    @Autowired
    ClassRepository classRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AgrupamentoUserClassRepository agrupamentoRepository;


    public Class createClass(ClassRegisterDTO classRegisterDTO) {
        if(userRepository.getTipoUsuario(classRegisterDTO.getTeacherId()) != 2){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de usuario informado não é um professor.");
        }

        String uniqueClassID = uniqueIDGenerate(System.currentTimeMillis());

        String RGBColorString = findRandomColor();

        while (classRepository.existsByClassCode(uniqueClassID)) {
            uniqueClassID = uniqueIDGenerate(System.currentTimeMillis());
        }

        log.info("UniqueClassID: {}", uniqueClassID);

        Class newClass = Class.builder()
                .className(classRegisterDTO.getClassName())
                .announcement(classRegisterDTO.getAnnouncement() != null ? classRegisterDTO.getAnnouncement() : null)
                .classCode(uniqueClassID)
                .teacherId(classRegisterDTO.getTeacherId())
                .color(RGBColorString)
                .build();

        Class newClassEntity = classRepository.save(newClass);

        AgrupamentoUserClass newAgrupamento = AgrupamentoUserClass.builder()
                .userId(classRegisterDTO.getTeacherId())
                .classId(newClassEntity.getClassId())
                .build();

        agrupamentoRepository.save(newAgrupamento);
        return newClassEntity;
    }

    private String findRandomColor(){
        String[] colors = {
                "170,71,188",
                "122,30,158",
                "120,144,156",
                "70,89,103",
                "236,64,122",
                "194,23,91",
                "92,107,189",
                "0,137,209",
                "0,87,156",
                "0,152,166",
                "0,137,119",
                "0,77,63",
                "104,159,57",
                "52,104,31",
                "140,110,99",
                "93,64,56",
                "126,88,191",
                "79,49,160",
                "241,107,0",
                "243,80,31",
                "193,53,5"
        };

        List<String> colorAsList = Arrays.asList(colors);

        Random rand = new Random();

        return colorAsList.get(rand.nextInt(colorAsList.size()));
    }

    private String uniqueIDGenerate(final long base10){
        if (base10 == 0)
            return "0";

        long temp = base10;
        final StringBuilder sb = new StringBuilder();

        while (temp > 0) {
            if(sb.length() == 6) break;
            temp = fromBase10(temp, sb);
        }
        return sb.reverse().toString();
    }

    private Long fromBase10(final long base10, final StringBuilder sb){
        final int rem = (int) (base10 % MIX_STRING_LENGTH);
        sb.append(MIX_STRING.charAt(rem));
        return base10 / MIX_STRING_LENGTH;
    }

    public AgrupamentoUserClass linkUserToClass(Long userId, String classCode) {
        Optional<Class> classFound = classRepository.findByClassCode(classCode);

        if(classFound.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo de classe inexistente.");
        }
        if (!userRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID inexistente.");
        }

        if(agrupamentoRepository.findByUserIdAndClassId(userId, classFound.get().getClassId()).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O aluno já está vinculdo a essa turma.");
        }

        AgrupamentoUserClass agrupamentoUserClass = AgrupamentoUserClass.builder()
                .classId(classFound.get().getClassId())
                .userId(userId)
                .build();

        return agrupamentoRepository.save(agrupamentoUserClass);
    }

    public List<ClassesInfoDTO> getClassesFromUser(Long userId) {
        List<ClassesInfoDTO> listClassesInfo = new ArrayList<>();

        List<AgrupamentoUserClass> listAgrupamento = agrupamentoRepository.findByUserId(userId);

        listAgrupamento.forEach(agrupamento -> {
            Optional<Class> optionalClassFromUser = classRepository.findById(agrupamento.getClassId());
            Class classFromUser;
            if(optionalClassFromUser.isPresent()){
                classFromUser = optionalClassFromUser.get();
                String teacherName = userRepository.getUserName(classFromUser.getTeacherId());

                ClassesInfoDTO classInfo = ClassesInfoDTO.builder()
                        .classId(classFromUser.getClassId())
                        .className(classFromUser.getClassName())
                        .announcement(classFromUser.getAnnouncement())
                        .classCode(classFromUser.getClassCode())
                        .teacher_id(classFromUser.getTeacherId())
                        .teacherName(teacherName)
                        .color(classFromUser.getColor())
                        .teacherFirstLetter(teacherName.substring(0,1).toUpperCase())
                        .build();
                
                listClassesInfo.add(classInfo);
            } else{
                log.info("Class de agrupamento nao existe. Class Id: {}", agrupamento.getClassId());
                agrupamentoRepository.deleteById(agrupamento.getUserClassId());
            }
        });

        return listClassesInfo;
    }

    public Class updateClass(Long classId, ClassRegisterDTO classRegisterDTO, Long teacherIdToken) {
        log.info("Atualizando classe...");
        Optional<Class> actualClass = classRepository.findById(classId);

        if(actualClass.isPresent()){

            if(actualClass.get().getTeacherId() != teacherIdToken){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Professor não é dono da turma que está tentando editar");
            }
            ModelMapper modelMapper = new ModelMapper();
            Class updatedClass = actualClass.get();
            modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
            modelMapper.map(classRegisterDTO, updatedClass);
            return classRepository.save(updatedClass);
        }

        log.info("Classe não encontrada para atualizar");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Classe não encontrada.");
    }

    public ClassesInfoDTO getClassById(Long userId, Long classId) {

        Optional<Class> optionalClassFromUser = classRepository.findById(classId);

        Class classFromUser;
        if(optionalClassFromUser.isPresent()){
            classFromUser = optionalClassFromUser.get();
            String teacherName = userRepository.getUserName(classFromUser.getTeacherId());

            ClassesInfoDTO classInfo = ClassesInfoDTO.builder()
                    .classId(classFromUser.getClassId())
                    .className(classFromUser.getClassName())
                    .announcement(classFromUser.getAnnouncement())
                    .classCode(classFromUser.getClassCode())
                    .teacher_id(classFromUser.getTeacherId())
                    .teacherName(teacherName)
                    .color(classFromUser.getColor())
                    .teacherFirstLetter(teacherName.substring(0,1).toUpperCase())
                    .build();

            Optional<AgrupamentoUserClass> optionalAgrupamentoUserClass = agrupamentoRepository.findByUserIdAndClassId(userId, classInfo.getClassId());
            if(optionalAgrupamentoUserClass.isEmpty()){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O aluno não entrou na turma.");
            }
            return classInfo;
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Classe não encontrada");
        }
    }

    public List<ClassStudentsDTO> getStudentsFromClass(Long classId) {
        List<ClassStudentsDTO> classStudentsDTOList = new ArrayList<>();


        List<AgrupamentoUserClass> listAgrupamento = agrupamentoRepository.findByClassId(classId);
        Optional<Class> optionalClass = classRepository.findById(classId);

        if(optionalClass.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Classe não encontrada ou não existe.");
        }


        if(listAgrupamento.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não há usuarios vinculados a turma.");
        }

        listAgrupamento.forEach(agrupamentoUserClass -> {
            if(optionalClass.get().getTeacherId() == agrupamentoUserClass.getUserId()){
                return;
            }

            UsuarioDTO usuarioDTO = userRepository.getDadosUsuario(agrupamentoUserClass.getUserId());

            if(usuarioDTO == null){
                return;
            }

            classStudentsDTOList.add(ClassStudentsDTO
                    .builder()
                    .studentColor(usuarioDTO.getColor())
                    .studentEmail(usuarioDTO.getEmail())
                    .studentName(usuarioDTO.getName())
                    .studentFirstLetter(usuarioDTO.getName().substring(0,1).toUpperCase())
                    .build()
            );


        });

        return classStudentsDTOList;
    }
}
