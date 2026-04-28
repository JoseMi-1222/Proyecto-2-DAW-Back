package com.ies.poligono.sur.app.horario.dto;

import com.ies.poligono.sur.app.horario.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	private String token;
	private Usuario usuario;
}