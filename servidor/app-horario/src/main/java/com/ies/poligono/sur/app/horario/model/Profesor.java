package com.ies.poligono.sur.app.horario.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "profesor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Profesor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idProfesor;

	@OneToOne
	@JoinColumn(name = "id_usuario", nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "profesor" })
	private Usuario usuario;

	private String nombre;

	@Column(unique = true)
	private String abreviatura;
	
	
	@OneToMany(mappedBy = "profesor")
	@JsonIgnoreProperties({ "profesor", "hibernateLazyInitializer", "handler" })
	private List<Horario> horarios;
}	