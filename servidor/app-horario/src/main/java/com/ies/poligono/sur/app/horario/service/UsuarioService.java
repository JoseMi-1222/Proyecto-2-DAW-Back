package com.ies.poligono.sur.app.horario.service;

import java.util.List;
import com.ies.poligono.sur.app.horario.model.Usuario;

public interface UsuarioService {

	List<Usuario> obtenerUsuarios();

	Usuario crearUsuario(Usuario usuario);

	void eliminarUsuario(Long id);

	Usuario actualizarUsuario(Long id, Usuario usuarioActualizado);

	Usuario actualizarContraseña(Long id, String contrasenaActual, String nuevaContraseña);

	String generarEmailDesdeNombre(String nombre);

}