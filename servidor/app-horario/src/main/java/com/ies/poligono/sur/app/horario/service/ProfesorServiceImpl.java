package com.ies.poligono.sur.app.horario.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ies.poligono.sur.app.horario.dao.ProfesorRepository;
import com.ies.poligono.sur.app.horario.model.Profesor;

@Service
public class ProfesorServiceImpl implements ProfesorService {

	private static final Logger log = LoggerFactory.getLogger(ProfesorServiceImpl.class);

	@Autowired
	private ProfesorRepository profesorRepository;

	@Override
	@Transactional(readOnly = true)
	public Profesor findByNombre(String nombre) {
		return profesorRepository.findByNombre(nombre);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Profesor> buscarPorNombreParcial(String nombre) {
		return profesorRepository.findByNombreContainingIgnoreCase(nombre);
	}

	@Override
	@Transactional(readOnly = true)
	public Profesor findById(Long id) {
		return profesorRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Profesor> obtenerTodos() {
		return profesorRepository.findAll();
	}

	@Override
	@Transactional
	public Profesor insertar(Profesor profesor) {
		log.info("Guardando profesor: {}", profesor.getNombre());
		return profesorRepository.save(profesor);
	}

	@Override
	@Transactional(readOnly = true)
	public Profesor findByEmailUsuario(String email) {
		return profesorRepository.findByUsuario_Email(email).orElseThrow(() -> {
			log.error("Error buscando profesor con email: {}", email);
			return new RuntimeException("Profesor no encontrado para el email: " + email);
		});
	}

	@Override
	@Transactional(readOnly = true)
	public Long obtenerIdProfesorPorUsername(String email) {
		Optional<Profesor> profesor = profesorRepository.findByUsuario_Email(email);

		if (profesor.isEmpty()) {
			log.warn("→ No se encontró ningún profesor para el email: {}", email);
			return null;
		}

		Long id = profesor.get().getIdProfesor();
		log.info("→ ID del profesor encontrado para {}: {}", email, id);
		return id;
	}

	@Override
	@Transactional(readOnly = true)
	public Profesor findByIdUsuario(Long idUsuario) {
		return profesorRepository.findByUsuario_Id(idUsuario).orElse(null);
	}
}