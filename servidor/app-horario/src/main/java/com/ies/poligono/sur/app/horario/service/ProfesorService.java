package com.ies.poligono.sur.app.horario.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ies.poligono.sur.app.horario.model.Profesor;

public interface ProfesorService {

    Profesor findByNombre(String nombre);

    List<Profesor> buscarPorNombreParcial(String nombre);

    Profesor findById(Long id);

    List<Profesor> obtenerTodos();

    Profesor insertar(Profesor profesor);

    Profesor findByEmailUsuario(String email);

    Long obtenerIdProfesorPorUsername(String email);

    Profesor findByIdUsuario(Long idUsuario);

    Page<Profesor> obtenerProfesoresPaginados(String busqueda, Pageable pageable);
    
    void deleteById(Long id);
    
    Profesor actualizar(Long id, Profesor profesorDatos);

    List<Profesor> obtenerProfesoresSinUsuario();

    Profesor crearUsuarioParaProfesor(Long idProfesor, String email, String password);

}