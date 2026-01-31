package com.ies.poligono.sur.app.horario.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.ies.poligono.sur.app.horario.dao.UsuarioRepository;
import com.ies.poligono.sur.app.horario.dto.CambioContrasenaDTO;
import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.model.Usuario;
import com.ies.poligono.sur.app.horario.service.ProfesorService;
import com.ies.poligono.sur.app.horario.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	@Autowired
	UsuarioService usuarioService;

	@Autowired
	private ProfesorService profesorService;

	@Autowired
	UsuarioRepository usuarioRepository;

	@GetMapping
	public List<Usuario> obtenerUsuarios() {
		return usuarioService.obtenerUsuarios();
	}

	@PostMapping("/crear-con-profesor/{idProfesor}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<?> crearUsuarioYVincularAProfesor(@PathVariable Long idProfesor,
			@Valid @RequestBody Usuario usuario, BindingResult result) {

		if (result.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			result.getAllErrors().forEach(error -> {
				String fieldName = ((org.springframework.validation.FieldError) error).getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});
			return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
		}

		Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);

		Profesor profesor = profesorService.findById(idProfesor);
		if (profesor == null) {
			return new ResponseEntity<>("Profesor no encontrado", HttpStatus.NOT_FOUND);
		}

		profesor.setUsuario(nuevoUsuario);
		profesorService.insertar(profesor);

		return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminarUsuario(@PathVariable Long id) {
		usuarioService.eliminarUsuario(id);
	}

	@PutMapping("/{id_usuario}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<?> actualizarUsuario(@PathVariable Long id_usuario,
			@Valid @RequestBody Usuario usuarioActualizado) {

		try {
			Usuario usuarioActualizadoDesdeServicio = usuarioService.actualizarUsuario(id_usuario, usuarioActualizado);
			return ResponseEntity.ok(usuarioActualizadoDesdeServicio);

		} catch (RuntimeException e) {
			return ResponseEntity.status(400).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al actualizar el usuario");
		}
	}

	@PutMapping("/{id}/cambiar-contrasena") 
	@PreAuthorize("isAuthenticated()") 
	public ResponseEntity<?> cambiarContrasena(@PathVariable Long id, @RequestBody CambioContrasenaDTO dto,
			Authentication authentication) {

		Usuario usuarioLogueado = usuarioRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario logueado no encontrado"));

		if (!usuarioLogueado.getId().equals(id)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes cambiar la contraseña de otro usuario");
		}
		
		try {
			Usuario actualizado = usuarioService.actualizarContraseña(id, dto.getContrasenaActual(), dto.getNuevaContrasena());
			return ResponseEntity.ok(actualizado);
			
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
		}
	}
}