package com.ies.poligono.sur.app.horario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.service.ProfesorService;

@RestController
@RequestMapping("/api/profesores")
public class ProfesorController {

	@Autowired
	private ProfesorService profesorService;

	@GetMapping("/buscar")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'PROFESOR')")
	public ResponseEntity<List<Profesor>> buscarPorNombre(@RequestParam("nombre") String nombre) {
		List<Profesor> profesores = profesorService.buscarPorNombreParcial(nombre);
		return ResponseEntity.ok(profesores);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'PROFESOR')")
	public ResponseEntity<Profesor> obtenerPorId(@PathVariable Long id) {
		Profesor profesor = profesorService.findById(id);
		if (profesor != null) {
			return ResponseEntity.ok(profesor);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'PROFESOR')")
	public ResponseEntity<List<Profesor>> obtenerTodos() {
		List<Profesor> profesores = profesorService.obtenerTodos();
		return ResponseEntity.ok(profesores);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<Profesor> crearProfesor(@RequestBody Profesor profesor) {
		try {
			Profesor nuevo = profesorService.insertar(profesor);
			return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/usuario/{email}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'PROFESOR')")
	public ResponseEntity<Long> obtenerIdProfesorPorEmail(@PathVariable String email) {
		Long id = profesorService.obtenerIdProfesorPorUsername(email);
		if (id != null) {
			return ResponseEntity.ok(id);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}