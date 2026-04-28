package com.ies.poligono.sur.app.horario.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ies.poligono.sur.app.horario.dao.HorarioRepository;
import com.ies.poligono.sur.app.horario.dao.ProfesorRepository;
import com.ies.poligono.sur.app.horario.model.Horario;
import com.ies.poligono.sur.app.horario.model.Profesor;

@Service
public class HorarioServiceImpl implements HorarioService {

	@Autowired
	HorarioRepository horarioRepository;
	
	@Autowired
	ProfesorRepository profesorRepository;

	@Override
	public Horario crearHorario(Horario horario) {
		return horarioRepository.save(horario);
	}

	@Override
	public void borrarTodosLosHorarios() {
		horarioRepository.deleteAll();
	}

	@Override
	public List<Horario> obtenerTodos() {
		return horarioRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Horario> obtenerPorProfesor(Long idProfesor) {
		return horarioRepository.findByProfesor_IdProfesor(idProfesor);
	}

	@Override
	@Transactional
	public List<Horario> guardarHorarioProfesor(Long idProfesor, List<Horario> nuevosHorarios) {
		Profesor profesor = profesorRepository.findById(idProfesor)
				.orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

		horarioRepository.deleteByProfesor_IdProfesor(idProfesor);
		horarioRepository.flush();

		for (Horario h : nuevosHorarios) {
			h.setProfesor(profesor);
		}

		return horarioRepository.saveAll(nuevosHorarios);
	}
}