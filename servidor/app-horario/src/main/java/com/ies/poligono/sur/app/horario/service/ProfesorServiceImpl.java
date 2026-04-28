package com.ies.poligono.sur.app.horario.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ies.poligono.sur.app.horario.dao.HorarioRepository; 
import com.ies.poligono.sur.app.horario.dao.ProfesorRepository;
import com.ies.poligono.sur.app.horario.dao.UsuarioRepository;
import com.ies.poligono.sur.app.horario.model.Profesor;
import com.ies.poligono.sur.app.horario.model.Usuario;

@Service
public class ProfesorServiceImpl implements ProfesorService {

    private static final Logger log = LoggerFactory.getLogger(ProfesorServiceImpl.class);

    @Autowired
    private ProfesorRepository profesorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HorarioRepository horarioRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Profesor findByNombre(String nombre) {
        return profesorRepository.findByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profesor> buscarPorNombreParcial(String nombre) {
        return profesorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public Profesor findById(Long id) {
        return profesorRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profesor> obtenerTodos() {
        return profesorRepository.findAll();
    }

    @Override
    @Transactional
    public Profesor insertar(Profesor profesor) {
        log.info("Guardando profesor: {}", profesor.getNombre());
        return profesorRepository.save(profesor);
    }

    @Override
    @Transactional(readOnly = true)
    public Profesor findByEmailUsuario(String email) {
        return profesorRepository.findByUsuario_Email(email).orElseThrow(() -> {
            log.error("Error buscando profesor con email: {}", email);
            return new RuntimeException("Profesor no encontrado para el email: " + email);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerIdProfesorPorUsername(String email) {
        Optional<Profesor> profesor = profesorRepository.findByUsuario_Email(email);

        if (profesor.isEmpty()) {
            log.warn("→ No se encontró ningún profesor para el email: {}", email);
            return null;
        }

        Long id = profesor.get().getIdProfesor();
        log.info("→ ID del profesor encontrado para {}: {}", email, id);
        return id;
    }

    @Override
    @Transactional(readOnly = true)
    public Profesor findByIdUsuario(Long idUsuario) {
        return profesorRepository.findByUsuario_Id(idUsuario).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Profesor> obtenerProfesoresPaginados(String busqueda, Pageable pageable) {
        Page<Profesor> profesores = profesorRepository.buscarProfesoresConFiltro(busqueda, pageable);
        
        profesores.forEach(p -> p.getHorarios().size());
        
        return profesores;
    }
    
    @Override
    @Transactional 
    public void deleteById(Long id) {
        Profesor profesor = findById(id);
        
        if (profesor != null) {
            Usuario usuarioAsociado = profesor.getUsuario();
            
            horarioRepository.deleteByProfesor_IdProfesor(id);
            log.info("Horarios eliminados para el profesor id: {}", id);
            
            profesorRepository.delete(profesor);
            log.info("Profesor eliminado: {}", profesor.getNombre());
            
            if (usuarioAsociado != null) {
                log.info("Borrando usuario asociado: {}", usuarioAsociado.getEmail());
                usuarioRepository.delete(usuarioAsociado);
            }
        } else {
             throw new RuntimeException("No se puede borrar. Profesor no encontrado con ID: " + id);
        }
    }
    
    @Override
    @Transactional
    public Profesor actualizar(Long id, Profesor profesorDatos) {
        Profesor existente = findById(id);
        if (existente != null) {
            existente.setNombre(profesorDatos.getNombre());
            return profesorRepository.save(existente);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profesor> obtenerProfesoresSinUsuario() {
        return profesorRepository.findByUsuarioIsNull();
    }

    @Override
    @Transactional
    public Profesor crearUsuarioParaProfesor(Long idProfesor, String email, String password) {
        Profesor profesor = findById(idProfesor);
        if (profesor == null) {
            throw new RuntimeException("Profesor no encontrado");
        }
        
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setNombre(profesor.getNombre());
        nuevoUsuario.setRol("profesor"); 
        
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);

        profesor.setUsuario(nuevoUsuario);
        return profesorRepository.save(profesor);
    }

    @Override
    @Transactional(readOnly = true)
    public Profesor findByAbreviatura(String abreviatura) {
        return profesorRepository.findByAbreviatura(abreviatura);
    }
    
    @Override
	@Transactional
	public Profesor crearProfesorYUsuario(com.ies.poligono.sur.app.horario.dto.CrearProfesorUsuarioDTO dto) {
		
		if (profesorRepository.findByNombre(dto.getNombre()) != null) {
			throw new RuntimeException("Ya existe un profesor con el nombre exacto: " + dto.getNombre() + ". Añade un segundo apellido o una inicial para diferenciarlo.");
		}

		if (profesorRepository.findByAbreviatura(dto.getAbreviatura()) != null) {
			throw new RuntimeException("Ya existe un profesor con la abreviatura: " + dto.getAbreviatura());
		}
		if (usuarioRepository.existsByEmail(dto.getEmail())) {
			throw new RuntimeException("El email ya está en uso por otro usuario.");
		}

		Usuario nuevoUsuario = new Usuario();
		nuevoUsuario.setNombre(dto.getNombre());
		nuevoUsuario.setEmail(dto.getEmail());
		nuevoUsuario.setRol("profesor");
		
		nuevoUsuario.setPassword(passwordEncoder.encode("Cambiame123!")); 
		
		nuevoUsuario = usuarioRepository.save(nuevoUsuario);

		Profesor nuevoProfesor = new Profesor();
		nuevoProfesor.setNombre(dto.getNombre());
		nuevoProfesor.setAbreviatura(dto.getAbreviatura());
		nuevoProfesor.setUsuario(nuevoUsuario);

		return profesorRepository.save(nuevoProfesor);
	}
    
    @Override
	@Transactional
	public void cambiarEstadoProfesor(Long idProfesor, boolean estado) {
		Profesor profesor = findById(idProfesor);
		
		if (profesor != null) {
			profesor.setActivo(estado);
			if (profesor.getUsuario() != null) {
				profesor.getUsuario().setActivo(estado);
			}
			
			if (estado == true) { 
				
				if (profesor.getSustitutoDe() != null) {
					Profesor original = profesor.getSustitutoDe();
					original.setActivo(false);
					if (original.getUsuario() != null) {
						original.getUsuario().setActivo(false);
					}
					profesorRepository.save(original);
					log.info("Apagado automático: {} era el original", original.getNombre());
				}
				
				List<Profesor> susSustitutos = profesorRepository.findBySustitutoDe(profesor);
				for (Profesor sust : susSustitutos) {
					if (sust.getActivo()) {
						sust.setActivo(false);
						if (sust.getUsuario() != null) {
							sust.getUsuario().setActivo(false);
						}
						profesorRepository.save(sust);
						log.info("Apagado automático: {} era el sustituto", sust.getNombre());
					}
				}
			}
			
			profesorRepository.save(profesor);
			log.info("El estado del profesor {} ha cambiado a activo={}", profesor.getNombre(), estado);
		} else {
			throw new RuntimeException("Profesor no encontrado");
		}
	}
    
    @Override
	@Transactional(readOnly = true)
	public Page<Profesor> obtenerProfesoresPaginados(String busqueda, boolean activo, Pageable pageable) {
		Page<Profesor> profesores = profesorRepository.buscarProfesoresConFiltroYEstado(busqueda, activo, pageable);
		
		profesores.forEach(p -> p.getHorarios().size());
		return profesores;
	}
    
    @Override
	@Transactional
	public Profesor crearSustituto(Long idProfesorOriginal, com.ies.poligono.sur.app.horario.dto.CrearProfesorUsuarioDTO dtoSustituto) {
		
		Profesor profesorOriginal = findById(idProfesorOriginal);
		if (profesorOriginal == null) {
			throw new RuntimeException("Profesor original no encontrado.");
		}

		Profesor profesorSustituto = crearProfesorYUsuario(dtoSustituto);

		List<com.ies.poligono.sur.app.horario.model.Horario> horariosOriginales = horarioRepository.findByProfesor_IdProfesor(idProfesorOriginal);
		
		for (com.ies.poligono.sur.app.horario.model.Horario horarioOriginal : horariosOriginales) {
			com.ies.poligono.sur.app.horario.model.Horario nuevoHorario = new com.ies.poligono.sur.app.horario.model.Horario();
			
			nuevoHorario.setDia(horarioOriginal.getDia());
			nuevoHorario.setFranja(horarioOriginal.getFranja());
			nuevoHorario.setAsignatura(horarioOriginal.getAsignatura());
			nuevoHorario.setAula(horarioOriginal.getAula());
			nuevoHorario.setCurso(horarioOriginal.getCurso());
			
			nuevoHorario.setProfesor(profesorSustituto);
			
			horarioRepository.save(nuevoHorario);
		}

		cambiarEstadoProfesor(idProfesorOriginal, false);

		log.info("Sustituto {} creado con éxito copiando el horario de {}", profesorSustituto.getNombre(), profesorOriginal.getNombre());
		
		profesorSustituto.setSustitutoDe(profesorOriginal);
		profesorRepository.save(profesorSustituto);
				
		return profesorSustituto;
	}
}