package com.ies.poligono.sur.app.horario.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ies.poligono.sur.app.horario.dao.AsignaturaRepository;
import com.ies.poligono.sur.app.horario.dao.AulaRepository;
import com.ies.poligono.sur.app.horario.dao.CursoRepository;
import com.ies.poligono.sur.app.horario.dao.HorarioRepository;
import com.ies.poligono.sur.app.horario.dao.ProfesorRepository;

import com.ies.poligono.sur.app.horario.dto.BackupDataDTO;

import com.ies.poligono.sur.app.horario.model.Horario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class BackupService {

    @Autowired private HorarioRepository horarioRepository;
    @Autowired private ProfesorRepository profesorRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private AsignaturaRepository asignaturaRepository;
    @Autowired private AulaRepository aulaRepository;


    public BackupDataDTO exportarDatosCompletos() {
        BackupDataDTO backup = new BackupDataDTO();
        backup.setProfesores(profesorRepository.findAll());
        backup.setCursos(cursoRepository.findAll());
        backup.setAsignaturas(asignaturaRepository.findAll());
        backup.setAulas(aulaRepository.findAll());
        backup.setHorarios(horarioRepository.findAll());
        return backup;
    }

    public String generarTxtHorario() {
        List<Horario> horarios = horarioRepository.findAll();
        StringBuilder sb = new StringBuilder();

        for (Horario h : horarios) {
            String asig = h.getAsignatura() != null ? h.getAsignatura().getNombre() : "";
            String curso = h.getCurso() != null ? h.getCurso().getNombre() : "";
            String aula = h.getAula() != null ? h.getAula().getCodigo() : ""; // Ojo: getCodigo() o getNombre() según tu modelo Aula
            String profe = h.getProfesor() != null ? h.getProfesor().getNombre() : "";
            
            String dia = convertirDiaLetra(h.getDia()); 
            String hora = h.getFranja() != null ? String.valueOf(h.getFranja().getIdFranja()) : "";

            sb.append(asig).append("\t")
              .append(curso).append("\t")
              .append(aula).append("\t")
              .append(profe).append("\t")
              .append(dia).append("\t")
              .append(hora).append("\n");
        }
        return sb.toString();
    }

    public void procesarImportacionJson(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BackupDataDTO datos = mapper.readValue(file.getInputStream(), BackupDataDTO.class);
        importarDatos(datos);
    }

    @Transactional
    public void importarDatos(BackupDataDTO datos) {
        horarioRepository.deleteAll(); 
        profesorRepository.deleteAll();
        cursoRepository.deleteAll();
        asignaturaRepository.deleteAll();
        aulaRepository.deleteAll();

        if (datos.getCursos() != null) cursoRepository.saveAll(datos.getCursos());
        if (datos.getAsignaturas() != null) asignaturaRepository.saveAll(datos.getAsignaturas());
        if (datos.getAulas() != null) aulaRepository.saveAll(datos.getAulas());
        if (datos.getProfesores() != null) profesorRepository.saveAll(datos.getProfesores());
        if (datos.getHorarios() != null) horarioRepository.saveAll(datos.getHorarios());
    }

    private String convertirDiaLetra(String dia) {
        if (dia == null) return "";
        dia = dia.trim().toUpperCase();
        if (dia.contains("MIERCOLES") || dia.contains("MIÉRCOLES")) return "X";
        return dia.substring(0, 1);
    }
}