package com.ies.poligono.sur.app.horario.controller;

import java.util.ArrayList;
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
import com.ies.poligono.sur.app.horario.dao.CursoRepository;
import com.ies.poligono.sur.app.horario.dao.FranjaRepository;
import com.ies.poligono.sur.app.horario.dao.ProfesorRepository;
import com.ies.poligono.sur.app.horario.dao.AulaRepository;
import com.ies.poligono.sur.app.horario.dto.HorarioDTO;
import com.ies.poligono.sur.app.horario.model.Asignatura;
import com.ies.poligono.sur.app.horario.model.Curso;
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

	@Autowired
	private FranjaRepository franjaRepository;

	@Autowired
	private ProfesorRepository profesorRepository;

	@Autowired
	private AulaRepository aulaRepository;

	@GetMapping("/profesor/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR') or hasAuthority('administrador') or hasAuthority('profesor')")
	public ResponseEntity<List<Horario>> obtenerHorario(@PathVariable Long id) {
		return ResponseEntity.ok(horarioService.obtenerPorProfesor(id));
	}

	@PostMapping("/profesor/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('administrador')")
	public ResponseEntity<List<Horario>> guardarHorario(@PathVariable Long id, @RequestBody List<HorarioDTO> horariosDTO) {
		
		List<Horario> horarios = new ArrayList<>();

		for (HorarioDTO dto : horariosDTO) {
			Horario horario = new Horario();
			
			horario.setDia(dto.getDia());
			
			horario.setFranja(franjaRepository.findById(dto.getIdFranja()).orElse(null));
			horario.setAsignatura(asignaturaRepository.findById(dto.getIdAsignatura()).orElse(null));
			
			horario.setProfesor(profesorRepository.findById(id).orElse(null));

			if (dto.getIdCurso() != null) {
				horario.setCurso(cursoRepository.findById(dto.getIdCurso()).orElse(null));
			}
			
			if (dto.getIdAula() != null) {
				horario.setAula(aulaRepository.findById(dto.getIdAula()).orElse(null));
			}

			horarios.add(horario);
		}

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