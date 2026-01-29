package com.ies.poligono.sur.app.horario.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ies.poligono.sur.app.horario.dao.UsuarioRepository;
import com.ies.poligono.sur.app.horario.model.Usuario;

@Component
@Order(2)
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		Usuario admin = usuarioRepository.findByEmail("admin@admin.com");

		if (admin == null) {
			admin = new Usuario();
			admin.setEmail("admin@admin.com");
		}
		admin.setNombre("Administrador");
		admin.setPassword(passwordEncoder.encode("admin"));
		admin.setRol("administrador");

		usuarioRepository.save(admin);
	}
}