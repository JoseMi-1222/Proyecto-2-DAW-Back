package com.ies.poligono.sur.app.horario.dao;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ies.poligono.sur.app.horario.model.Horario;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
	
	List<Horario> findByProfesor_IdProfesor(Long idProfesor);
	
	void deleteByProfesor_IdProfesor(Long idProfesor);

	@Query("""
			SELECT h FROM Horario h
			JOIN h.franja f
			WHERE h.profesor.idProfesor = :idProfesor
			AND h.dia = :dia
			AND f.horaInicio >= :horaInicio
			AND f.horaInicio < :horaFin
			""")
	List<Horario> findHorariosEntreHoras(@Param("idProfesor") Long idProfesor, @Param("dia") String dia,
			@Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin);

}