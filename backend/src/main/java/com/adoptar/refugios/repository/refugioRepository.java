package com.adoptar.refugios.repository;

import com.adoptar.refugios.dominio.estadoverificacion;
import com.adoptar.refugios.dominio.refugio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefugioRepository extends JpaRepository<refugio, Long> {
    
    // Para el panel de administración (buscar los pendientes)
    List<refugio> findByEstadoVerificacion(EstadoVerificacion estado);
    
    // Para validar si un CUIT ya existe
    boolean existsByCuit(String cuit);
}