package com.ies.poligono.sur.app.horario.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ies.poligono.sur.app.horario.dto.PostImportacionInputDTO;
import com.ies.poligono.sur.app.horario.model.Asignatura;
import com.ies.poligono.sur.app.horario.model.Aula;
import com.ies.poligono.sur.app.horario.model.Curso;
import com.ies.poligono.sur.app.horario.model.Franja;
import com.ies.poligono.sur.app.horario.model.Horario;
import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.model.Usuario;
import com.ies.poligono.sur.app.horario.service.AsignaturaService;
import com.ies.poligono.sur.app.horario.service.AulaService;
import com.ies.poligono.sur.app.horario.service.CursoService;
import com.ies.poligono.sur.app.horario.service.FranjaService;
import com.ies.poligono.sur.app.horario.service.HorarioService;
import com.ies.poligono.sur.app.horario.service.ProfesorService;
import com.ies.poligono.sur.app.horario.service.UsuarioService;

@Service
public class HorarioServiceProcessorImpl implements HorarioServiceProcessor {

	@Autowired
	HorarioService horarioService;

	@Autowired
	AsignaturaService asignaturaService;

	@Autowired
	CursoService cursoService;

	@Autowired
	AulaService aulaService;

	@Autowired
	ProfesorService profesorService;

	@Autowired
	UsuarioService usuarioService;

	@Autowired
	FranjaService franjaService;

	@Override
	public void importarHorario(PostImportacionInputDTO inputDTO) {

		byte[] decoded = Base64.getDecoder().decode(inputDTO.getFile());
		String decodedStr = new String(decoded, StandardCharsets.UTF_8);

		insertarHorarioImportado(montarLstHorarioDesdeTxt(decodedStr));
	}

	private List<Horario> montarLstHorarioDesdeTxt(String txtHorario) {
		List<Horario> lstHorario = new ArrayList<Horario>();

		try (BufferedReader reader = new BufferedReader(new StringReader(txtHorario))) {
			String txtFilaHorario;
			int lineNumber = 0;
			while ((txtFilaHorario = reader.readLine()) != null) {
				lineNumber++;
				Horario horario = montarRegistroDesdeFilaTxt(txtFilaHorario);
				if (horario != null) {
					lstHorario.add(horario);
				} else {
					System.err.println("Saltando línea " + lineNumber + " por errores de validación");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Total de horarios procesados correctamente: " + lstHorario.size());
		return lstHorario;
	}

	private Horario montarRegistroDesdeFilaTxt(String txtFilaHorario) {
		Horario horario = new Horario();

		String lineaLimpia = txtFilaHorario.trim();

		String[] arrHorario = lineaLimpia.split("\t");

		for (int i = 0; i < arrHorario.length; i++) {
			arrHorario[i] = arrHorario[i].trim();
		}

		if (arrHorario.length < 6) {
			return null;
		}

		Asignatura asignatura = procesarAsignatura(arrHorario[0]);
		horario.setAsignatura(asignatura);

		Curso curso = procesarCurso(arrHorario[1]);
		horario.setCurso(curso);

		Aula aula = procesarAula(arrHorario[2]);
		horario.setAula(aula);

		Profesor profesor = procesarProfesor(arrHorario[3]);
		horario.setProfesor(profesor);

		horario.setDia(arrHorario[4]);

		String franjaStr = arrHorario[5];
		if (franjaStr == null || franjaStr.isEmpty() || !franjaStr.matches("\\d+")) {
			System.err.println("ERROR: Franja inválida en línea: " + txtFilaHorario);
			return null;
		}

		Long idFranja = Long.valueOf(franjaStr);
		Franja franja = franjaService.findById(idFranja).orElse(null);

		if (franja == null) {
			System.err.println("ADVERTENCIA: No se encontró la franja con ID " + idFranja + " en la base de datos");
		}

		horario.setFranja(franja);

		return horario;
	}

	private Asignatura procesarAsignatura(String nombreAsignatura) {
		Asignatura asignatura = asignaturaService.findByNombre(nombreAsignatura);
		if (asignatura == null) {
			asignatura = new Asignatura();
			asignatura.setNombre(nombreAsignatura);
			asignatura = asignaturaService.insertar(asignatura);
		}
		return asignatura;
	}

	private Curso procesarCurso(String nombre) {
		Curso curso = null;
		if (StringUtils.isNotBlank(nombre)) {
			curso = cursoService.findByNombre(nombre);
			if (curso == null) {
				curso = new Curso();
				curso.setNombre(nombre);
				curso = cursoService.insertar(curso);
			}
		}
		return curso;
	}

	private Aula procesarAula(String codigo) {
		Aula aula = null;

		if (StringUtils.isNotBlank(codigo)) {
			aula = aulaService.findByCodigo(codigo);
			if (aula == null) {
				aula = new Aula();
				aula.setCodigo(codigo);
				aula = aulaService.insertar(aula);
			}
		}

		return aula;
	}

	private Profesor procesarProfesor(String codigoAbreviatura) {

		Profesor profesor = profesorService.findByAbreviatura(codigoAbreviatura);

		if (profesor == null) {
			System.out.println("Creando nuevo profesor con abreviatura: " + codigoAbreviatura);

			Usuario usuario = new Usuario();

			String email = usuarioService.generarEmailDesdeNombre(codigoAbreviatura);
			usuario.setEmail(email);
			usuario.setPassword("Pass123");
			usuario.setRol("profesor");
			usuario.setNombre(codigoAbreviatura);
			usuario = usuarioService.crearUsuario(usuario);

			profesor = new Profesor();
			profesor.setUsuario(usuario);
			profesor.setNombre(codigoAbreviatura);
			profesor.setAbreviatura(codigoAbreviatura);
			profesor = profesorService.insertar(profesor);
		}
		return profesor;
	}

	private void insertarHorarioImportado(List<Horario> lstHorario) {
		horarioService.borrarTodosLosHorarios();
		for (Horario horario : lstHorario) {
			horarioService.crearHorario(horario);
		}
	}
}