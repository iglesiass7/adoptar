package com.adoptar.usuarios.service;

import com.adoptar.shared.exception.RecursoNoEncontradoException;
import com.adoptar.usuarios.dominio.Adoptante;
import com.adoptar.usuarios.dominio.PerfilIncompletoException;
import com.adoptar.usuarios.dto.PerfilAdoptanteRequest;
import com.adoptar.usuarios.dto.PerfilAdoptanteResponse;
import com.adoptar.usuarios.repository.AdoptanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-04 · Completar el perfil del adoptante.
 * <p>
 * Otros módulos lo usan para saber si un adoptante puede enviar solicitudes (regla 4):
 * {@link #exigirPerfilCompleto(Long)} o {@link #tienePerfilCompleto(Long)}.
 */
@Service
public class PerfilAdoptanteService {

    private final AdoptanteRepository adoptantes;

    public PerfilAdoptanteService(AdoptanteRepository adoptantes) {
        this.adoptantes = adoptantes;
    }

    /**
     * Perfil de un adoptante.
     *
     * @throws RecursoNoEncontradoException si todavía no lo completó
     */
    @Transactional(readOnly = true)
    public PerfilAdoptanteResponse obtener(Long usuarioId) {
        return adoptantes.findById(usuarioId)
                .map(PerfilAdoptanteResponse::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de adoptante", usuarioId));
    }

    /**
     * Crea el perfil la primera vez y lo actualiza las siguientes.
     * El rol ADOPTANTE lo controla SecurityConfig antes de llegar acá.
     */
    @Transactional
    public PerfilAdoptanteResponse guardar(Long usuarioId, PerfilAdoptanteRequest datos) {
        Adoptante perfil = adoptantes.findById(usuarioId)
                .map(existente -> {
                    existente.actualizar(datos.fechaNacimiento(), datos.provincia(), datos.localidad(),
                            datos.tipoVivienda(), datos.tienePatio(), datos.otrasMascotas());
                    return existente;
                })
                .orElseGet(() -> adoptantes.save(new Adoptante(usuarioId, datos.fechaNacimiento(),
                        datos.provincia(), datos.localidad(), datos.tipoVivienda(), datos.tienePatio(),
                        datos.otrasMascotas())));
        return PerfilAdoptanteResponse.de(perfil);
    }

    @Transactional(readOnly = true)
    public boolean tienePerfilCompleto(Long usuarioId) {
        return adoptantes.existsById(usuarioId);
    }

    /**
     * Regla 4, para usar antes de crear una solicitud.
     *
     * @throws PerfilIncompletoException si el adoptante todavía no completó su perfil
     */
    @Transactional(readOnly = true)
    public void exigirPerfilCompleto(Long usuarioId) {
        if (!tienePerfilCompleto(usuarioId)) {
            throw new PerfilIncompletoException();
        }
    }
}
