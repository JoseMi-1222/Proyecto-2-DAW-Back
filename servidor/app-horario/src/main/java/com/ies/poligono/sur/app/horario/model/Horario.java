package com.ies.poligono.sur.app.horario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Cacheable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "horarios" })
public class Horario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_asignatura", nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Asignatura asignatura;

	@ManyToOne
	@JoinColumn(name = "id_curso", nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Curso curso;

	@ManyToOne
	@JoinColumn(name = "id_aula", nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Aula aula;

	@ManyToOne
	@JoinColumn(name = "id_profesor", nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Profesor profesor;

	@Column(nullable = false)
	private String dia;

	@ManyToOne
	@JoinColumn(name = "franja", nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Franja franja;

}