package com.adoptar.refugios.service;

import com.adoptar.refugios.dominio.estadoverificacion;
import com.adoptar.refugios.dominio.refugio;
import com.adoptar.refugios.repository.refugioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class refugioService {

    private final refugioRepository refugioRepository;

    public refugioService(refugioRepository refugioRepository) {
        this.refugioRepository = refugioRepository;
    }

    // CU-22: El admin ve los refugios pendientes
    public List<refugio> obtenerPendientes() {
        return refugioRepository.findByEstadoVerificacion(estadoverificacion.PENDIENTE);
    }

    // CU-23: El admin verifica un refugio
    public refugio verificarRefugio(Long usuarioId) {
        refugio refugio = refugioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Refugio no encontrado"));
        
        refugio.setEstadoVerificacion(estadoverificacion.VERIFICADO);
        return refugioRepository.save(refugio);
    }

    // CU-24: El admin rechaza un refugio
    public refugio rechazarRefugio(Long usuarioId) {
        refugio refugio = refugioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Refugio no encontrado"));
        
        refugio.setEstadoVerificacion(estadoverificacion.RECHAZADO);
        return refugioRepository.save(refugio);
    }
}