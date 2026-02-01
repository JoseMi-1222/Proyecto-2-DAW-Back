package com.ies.poligono.sur.app.horario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

	@GetMapping("/gestion")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<Page<Profesor>> obtenerProfesoresGestion(
			@RequestParam(required = false) String busqueda,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		Page<Profesor> profesores = profesorService.obtenerProfesoresPaginados(
			busqueda, 
			PageRequest.of(page, size, Sort.by("nombre").ascending())
		);
		return ResponseEntity.ok(profesores);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<Void> eliminarProfesor(@PathVariable Long id) {
		profesorService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Profesor> actualizarProfesor(@PathVariable Long id, @RequestBody Profesor profesor) {
        Profesor actualizado = profesorService.actualizar(id, profesor);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

	// --- MÉTODOS AÑADIDOS PARA ASIGNAR USUARIO ---

	@GetMapping("/sin-usuario")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<List<Profesor>> obtenerProfesoresSinUsuario() {
		return ResponseEntity.ok(profesorService.obtenerProfesoresSinUsuario());
	}

	@PostMapping("/{id}/asignar-usuario")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<?> asignarUsuario(@PathVariable Long id, @RequestBody RegistroUsuarioDTO datos) {
		try {
			Profesor actualizado = profesorService.crearUsuarioParaProfesor(id, datos.getEmail(), datos.getPassword());
			return ResponseEntity.ok(actualizado);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
		}
	}
	
	// DTO
	public static class RegistroUsuarioDTO {
		private String email;
		private String password;
		
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
	}
}