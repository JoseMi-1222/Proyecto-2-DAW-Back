package com.ies.poligono.sur.app.horario.apputils;

import java.time.LocalDate;

public class AusenciasUtils {

	public static String obtenerDiaSemanaByFecha(LocalDate fecha) {
		return switch (fecha.getDayOfWeek()) {
		case MONDAY -> "L";
		case TUESDAY -> "M";
		case WEDNESDAY -> "X";
		case THURSDAY -> "J";
		case FRIDAY -> "V";
		default -> throw new IllegalArgumentException("El día debe estar entre lunes y viernes");
		};
	}
}
