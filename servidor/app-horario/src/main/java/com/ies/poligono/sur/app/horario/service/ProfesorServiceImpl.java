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
        return profesorRepository.buscarProfesoresConFiltro(busqueda, pageable);
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
}