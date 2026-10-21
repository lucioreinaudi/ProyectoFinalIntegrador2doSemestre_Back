package com.example.demo.repositorio;

import com.example.demo.modelo.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoAlertaRepository extends JpaRepository<TipoAlerta, Long> {
    Optional<TipoAlerta> findByClave(String clave);

    boolean existsByClave(String clave);

    Optional<TipoAlerta> findByNombreIgnoreCase(String nombre);

    List<TipoAlerta> findAllByOrderByNombreAsc();
}
