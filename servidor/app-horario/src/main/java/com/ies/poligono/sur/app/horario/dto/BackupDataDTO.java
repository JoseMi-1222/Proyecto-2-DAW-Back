package com.ies.poligono.sur.app.horario.dto;

import java.util.List;
import com.ies.poligono.sur.app.horario.model.*; // Importa todos tus modelos
import lombok.Data; // Si usas Lombok. Si no, genera Getters/Setters

@Data // Si no usas Lombok, borra esto y genera los Getters y Setters abajo
public class BackupDataDTO {
    
    // Aquí definimos la estructura del archivo JSON
    private List<Curso> cursos;
    private List<Asignatura> asignaturas;
    private List<Aula> aulas;
    private List<Profesor> profesores;
    private List<Horario> horarios;
    
    // Si tuvieras usuarios sueltos (admin) que no son profesores:
    // private List<Usuario> usuarios;
}