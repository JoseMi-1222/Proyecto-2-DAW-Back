package com.ies.poligono.sur.app.horario.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ies.poligono.sur.app.horario.dto.AusenciaAgrupadaDTO;
import com.ies.poligono.sur.app.horario.dto.GuardiaDTO;
import com.ies.poligono.sur.app.horario.dto.PostAusenciasInputDTO;
import com.ies.poligono.sur.app.horario.model.Ausencia;
import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.service.AusenciaService;
import com.ies.poligono.sur.app.horario.service.FileStorageService;
import com.ies.poligono.sur.app.horario.service.ProfesorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ausencias")
@RequiredArgsConstructor
public class AusenciaController {

	@Autowired
	private AusenciaService ausenciaService;

	@Autowired
	private ProfesorService profesorService;

	@Autowired
	private FileStorageService fileStorageService;

	@GetMapping("/todas")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<List<Ausencia>> obtenerTodasLasAusencias() {
		return ResponseEntity.ok(ausenciaService.obtenerTodas());
	}

	@PostMapping("/upload-archivo")
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
	public ResponseEntity<String> uploadArchivo(@RequestParam("file") MultipartFile file) {
		String fileName = fileStorageService.guardarArchivo(file);
		return ResponseEntity.ok(fileName);
	}

	@GetMapping("/descargar-archivo/{fileName:.+}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
	public ResponseEntity<Resource> descargarArchivo(@PathVariable String fileName) {
		Resource resource = fileStorageService.cargarArchivoComoRecurso(fileName);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
	public ResponseEntity<?> crearAusencia(@RequestBody PostAusenciasInputDTO dto) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Set<String> roles = auth.getAuthorities().stream().map(r -> r.getAuthority()).collect(Collectors.toSet());
		Long idProfesor = null;
		if (roles.contains("ROLE_ADMINISTRADOR") && dto.getIdProfesor() != null) {
			idProfesor = dto.getIdProfesor();
		} else {
			String email = auth.getName();
			Profesor profesor = profesorService.findByEmailUsuario(email);
			idProfesor = profesor.getIdProfesor();
		}

		ausenciaService.crearAusenciaV2(dto, idProfesor);

		return ResponseEntity.ok().build();
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
	public ResponseEntity<List<AusenciaAgrupadaDTO>> obtenerAusencias(@RequestParam(required = false) Long idusuario,
			Principal principal) {

		Profesor profesor = null;

		if (idusuario != null) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Set<String> roles = auth.getAuthorities().stream().map(r -> r.getAuthority()).collect(Collectors.toSet());

			if (roles.contains("ROLE_ADMINISTRADOR")) {
				profesor = profesorService.findByIdUsuario(idusuario);
			} else {
				profesor = profesorService.findByEmailUsuario(principal.getName());
			}
		} else {
			profesor = profesorService.findByEmailUsuario(principal.getName());
		}

		List<AusenciaAgrupadaDTO> ausencias = ausenciaService.obtenerAusenciasAgrupadasV2(profesor.getIdProfesor());

		return ResponseEntity.ok(ausencias);
	}

	@DeleteMapping
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
	public ResponseEntity<Void> eliminarAusencia(@RequestBody java.util.Map<String, Object> payload,
			Principal principal) {

		if (payload.containsKey("id")) {
			Long id = Long.parseLong(payload.get("id").toString());
			ausenciaService.eliminarAusenciaPorId(id);
		} else if (payload.containsKey("fecha")) {
			String fechaStr = payload.get("fecha").toString();
			LocalDate fecha = LocalDate.parse(fechaStr);

			Long idProfesor = null;
			if (payload.containsKey("idProfesor")) {
				idProfesor = Long.parseLong(payload.get("idProfesor").toString());
			} else {
				idProfesor = profesorService.obtenerIdProfesorPorUsername(principal.getName());
			}

			ausenciaService.eliminarAusenciasPorFechaYProfesor(fecha, idProfesor);
		}

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/justificar-dia")
	@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
	public ResponseEntity<Void> justificarAusenciasDia(@RequestBody java.util.Map<String, Object> payload,
			Principal principal) {

		String fechaStr = payload.get("fecha").toString();
		LocalDate fecha = LocalDate.parse(fechaStr);

		Long idProfesor = null;
		if (payload.containsKey("idProfesor")) {
			idProfesor = Long.parseLong(payload.get("idProfesor").toString());
		} else {
			idProfesor = profesorService.obtenerIdProfesorPorUsername(principal.getName());
		}

		String nombreJustificante = null;
		if (payload.containsKey("justificante")) {
			nombreJustificante = payload.get("justificante").toString();
		}

		ausenciaService.justificarAusenciasDia(fecha, idProfesor, nombreJustificante);

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/aprobar-justificante")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<Void> aprobarJustificante(@RequestBody java.util.Map<String, Object> payload) {
		String fechaStr = payload.get("fecha").toString();
		LocalDate fecha = LocalDate.parse(fechaStr);
		Long idProfesor = Long.parseLong(payload.get("idProfesor").toString());

		ausenciaService.aprobarJustificante(fecha, idProfesor);

		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/guardias/hoy")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'PROFESOR')")
	public ResponseEntity<List<GuardiaDTO>> obtenerGuardiasDeHoy() {
		List<GuardiaDTO> guardias = ausenciaService.obtenerGuardiasDeHoy();
		return ResponseEntity.ok(guardias);
	}
}