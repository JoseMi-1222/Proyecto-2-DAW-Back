package com.ies.poligono.sur.app.horario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ies.poligono.sur.app.horario.dao.AsignaturaRepository;
import com.ies.poligono.sur.app.horario.dao.CursoRepository; // <--- NUEVO IMPORT
import com.ies.poligono.sur.app.horario.model.Asignatura;
import com.ies.poligono.sur.app.horario.model.Curso;      // <--- NUEVO IMPORT
import com.ies.poligono.sur.app.horario.model.Horario;
import com.ies.poligono.sur.app.horario.service.HorarioService;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;
    
    @Autowired
    private AsignaturaRepository asignaturaRepository; 

    @Autowired
    private CursoRepository cursoRepository;

    @GetMapping("/profesor/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR') or hasAuthority('administrador') or hasAuthority('profesor')")
    public ResponseEntity<List<Horario>> obtenerHorario(@PathVariable Long id) {
        return ResponseEntity.ok(horarioService.obtenerPorProfesor(id));
    }

    @PostMapping("/profesor/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('administrador')")
    public ResponseEntity<List<Horario>> guardarHorario(@PathVariable Long id, @RequestBody List<Horario> horarios) {
        return ResponseEntity.ok(horarioService.guardarHorarioProfesor(id, horarios));
    }
    
    @GetMapping("/asignaturas")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR') or hasAuthority('administrador') or hasAuthority('profesor')")
    public ResponseEntity<List<Asignatura>> obtenerTodasAsignaturas() {
        return ResponseEntity.ok(asignaturaRepository.findAll());
    }

    @GetMapping("/cursos")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR') or hasAuthority('administrador') or hasAuthority('profesor')")
    public ResponseEntity<List<Curso>> obtenerTodosCursos() {
        return ResponseEntity.ok(cursoRepository.findAll());
    }
}