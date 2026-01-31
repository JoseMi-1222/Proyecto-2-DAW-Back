package com.ies.poligono.sur.app.horario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Entity
@Table(name = "Usuario")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_usuario")
	private Long id;

	@NotNull(message = "El nombre no puede ser nulo")
	@Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
	private String nombre;

	@NotNull(message = "El email no puede ser nulo")
	@Email(message = "Debe tener un formato de email válido")
	@Column(name = "correo", unique = true)
	private String email;

	@NotNull(message = "La contraseña no puede ser nula")
	@Size(min = 6, message = "Debe tener al menos 6 caracteres")
	@Column(name = "password")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@NotNull(message = "El rol no puede ser nulo")
	@Pattern(regexp = "^(profesor|administrador)$", message = "El rol debe ser 'profesor' o 'administrador'")
	private String rol;
}