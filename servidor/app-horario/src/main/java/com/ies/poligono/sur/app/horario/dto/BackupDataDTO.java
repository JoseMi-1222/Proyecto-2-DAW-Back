package com.ies.poligono.sur.app.horario.dto;

import java.util.List;
import com.ies.poligono.sur.app.horario.model.*;
import lombok.Data;

@Data
public class BackupDataDTO {

	private List<Curso> cursos;
	private List<Asignatura> asignaturas;
	private List<Aula> aulas;
	private List<Profesor> profesores;
	private List<Horario> horarios;

}