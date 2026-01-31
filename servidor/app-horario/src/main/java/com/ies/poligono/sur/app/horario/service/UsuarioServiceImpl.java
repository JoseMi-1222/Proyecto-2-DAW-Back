package com.ies.poligono.sur.app.horario.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ies.poligono.sur.app.horario.dao.ProfesorRepository;
import com.ies.poligono.sur.app.horario.dao.UsuarioRepository;
import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.model.Usuario;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ProfesorRepository profesorRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final String DOMINIO_CORREO = "@iespoligonosur.org";

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> obtenerUsuarios() {
		return usuarioRepository.findAll();
	}

	@Override
	@Transactional
	public Usuario crearUsuario(Usuario usuario) {
		if (usuarioRepository.existsByEmail(usuario.getEmail())) {
			throw new IllegalArgumentException("Ya existe un usuario con ese email: " + usuario.getEmail());
		}

		String contraseñaEncriptada = passwordEncoder.encode(usuario.getPassword());
		usuario.setPassword(contraseñaEncriptada);

		log.info("Creando nuevo usuario: {}", usuario.getEmail());
		return usuarioRepository.save(usuario);
	}

	@Override
	@Transactional
	public void eliminarUsuario(Long id) {
		Usuario usuario = usuarioRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));

		Profesor profesor = profesorRepository.findByUsuario(usuario);

		if (profesor != null) {
			log.info("Desvinculando usuario {} del profesor {}", id, profesor.getNombre());
			profesor.setUsuario(null);
			profesorRepository.save(profesor);
		}

		log.info("Eliminando usuario con ID: {}", id);
		usuarioRepository.delete(usuario);
	}

	@Override
	@Transactional
	public Usuario actualizarUsuario(Long id_usuario, Usuario usuarioActualizado) {
		Usuario usuarioExistente = usuarioRepository.findById(id_usuario)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

		if (!usuarioExistente.getEmail().equals(usuarioActualizado.getEmail())
				&& usuarioRepository.existsByEmail(usuarioActualizado.getEmail())) {
			throw new RuntimeException("El correo electrónico ya está registrado por otro usuario");
		}

		if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
			usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
		}

		usuarioExistente.setNombre(usuarioActualizado.getNombre());
		usuarioExistente.setEmail(usuarioActualizado.getEmail());
		usuarioExistente.setRol(usuarioActualizado.getRol());

		return usuarioRepository.save(usuarioExistente);
	}

	@Override
	@Transactional
	public Usuario actualizarContraseña(Long id, String contrasenaActual, String nuevaContraseña) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

		if (!passwordEncoder.matches(contrasenaActual, usuario.getPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
		}

		String nuevaPass = passwordEncoder.encode(nuevaContraseña);
		usuario.setPassword(nuevaPass);
		log.info("Contraseña actualizada para el usuario ID: {}", id);
		return usuarioRepository.save(usuario);
	}

	@Override
	public String generarEmailDesdeNombre(String nombre) {
		if (nombre == null || !nombre.contains(",")) {
			return "";
		}

		String[] arrNombre = nombre.split(",");
		String apellidos = arrNombre[0].replace(".", "").trim();
		String nombreProf = "";

		if (arrNombre.length > 1) {
			nombreProf = arrNombre[1].trim().replace(".", "").concat(".");
		}

		String email = StringUtils
				.stripAccents(nombreProf.concat(apellidos).toLowerCase().replace(" ", ".").concat(DOMINIO_CORREO))
				.toLowerCase();

		return email;
	}
}