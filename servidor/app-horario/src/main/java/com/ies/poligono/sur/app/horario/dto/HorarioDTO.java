package com.ies.poligono.sur.app.horario.dto; 

import lombok.Data;

@Data
public class HorarioDTO {
    private String dia;
    private Long idFranja;
    private Long idAsignatura;
    private Long idProfesor;
    private Long idCurso; 
    private Long idAula; 
}