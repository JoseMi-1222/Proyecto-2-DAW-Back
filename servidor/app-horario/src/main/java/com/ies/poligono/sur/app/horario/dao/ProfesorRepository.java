package com.ies.poligono.sur.app.horario.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.model.Usuario;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {

	Profesor findByNombre(String nombre);

	List<Profesor> findByNombreContainingIgnoreCase(String nombre);

	Profesor findByUsuario(Usuario usuario);

	Optional<Profesor> findByUsuario_Email(String email);

	Optional<Profesor> findByUsuario_Id(Long usuarioId);

	@Query("SELECT DISTINCT p FROM Profesor p " +
	       "LEFT JOIN p.horarios h " +
	       "LEFT JOIN h.asignatura a " +
	       "WHERE (:busqueda IS NULL OR :busqueda = '' OR " +
	       "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
	       "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
	Page<Profesor> buscarProfesoresConFiltro(@Param("busqueda") String busqueda, Pageable pageable);
	
	List<Profesor> findByUsuarioIsNull();

	Profesor findByAbreviatura(String abreviatura);
	
	@Query("SELECT p FROM Profesor p WHERE (:busqueda IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND p.activo = :activo")
	Page<Profesor> buscarProfesoresConFiltroYEstado(String busqueda, boolean activo, Pageable pageable);
	
	List<Profesor> findBySustitutoDe(Profesor profesorOriginal);

}