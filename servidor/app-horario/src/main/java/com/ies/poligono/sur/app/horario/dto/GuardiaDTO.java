package com.ies.poligono.sur.app.horario.dto;

import lombok.Data;

@Data
public class GuardiaDTO {
	private String nombreProfesor;
	private String asignatura;
	private String aula;
	private String franja; 
	private String horaFin; 
}