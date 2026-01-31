package com.ies.poligono.sur.app.horario.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ies.poligono.sur.app.horario.dao.UsuarioRepository;
import com.ies.poligono.sur.app.horario.dto.AuthResponse;
import com.ies.poligono.sur.app.horario.model.AuthenticationRequest;
import com.ies.poligono.sur.app.horario.model.Usuario;
import com.ies.poligono.sur.app.horario.security.CustomUserDetailsService;
import com.ies.poligono.sur.app.horario.security.JwtService;

@RestController
@RequestMapping("/api")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtService jwtService;

	@PostMapping("/login")
	public AuthResponse loginUser(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
				authenticationRequest.getPassword()));

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

		final String jwt = jwtService.generateToken(userDetails);

		Usuario usuario = usuarioRepository.findByEmail(authenticationRequest.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

		return new AuthResponse(jwt, usuario);
	}
}