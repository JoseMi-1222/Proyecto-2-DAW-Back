package com.ies.poligono.sur.app.horario.service;

import java.time.LocalDate;
import java.util.List;

import com.ies.poligono.sur.app.horario.dto.AusenciaAgrupadaDTO;
import com.ies.poligono.sur.app.horario.dto.GuardiaDTO;
import com.ies.poligono.sur.app.horario.dto.PostAusenciasInputDTO;
import com.ies.poligono.sur.app.horario.model.Ausencia;

public interface AusenciaService {

	void crearAusencia(PostAusenciasInputDTO dto, Long idProfesor);

	void eliminarAusenciaPorId(Long id);

	List<AusenciaAgrupadaDTO> obtenerAusenciasAgrupadas(Long idProfesor);

	void eliminarAusenciasPorFechaYProfesor(LocalDate fecha, Long idProfesor);

	void crearAusenciaV2(PostAusenciasInputDTO dto, Long idProfesor);

	List<AusenciaAgrupadaDTO> obtenerAusenciasAgrupadasV2(Long idProfesor);

	void justificarAusenciasDia(LocalDate fecha, Long idProfesor, String nombreJustificante);
	
	void aprobarJustificante(LocalDate fecha, Long idProfesor);
	
	List<Ausencia> obtenerTodas();
	
	List<GuardiaDTO> obtenerGuardiasDeHoy();
	

}
