package com.rentaherramientas.infrastructure.catalog.config;

import com.rentaherramientas.application.catalog.port.AlmacenamientoImagenes;
import com.rentaherramientas.application.catalog.usecase.ActualizarCategoriaService;
import com.rentaherramientas.application.catalog.usecase.ActualizarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.ActualizarHerramientaService;
import com.rentaherramientas.application.catalog.usecase.ActualizarHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.CambiarEstadoHerramientaService;
import com.rentaherramientas.application.catalog.usecase.CambiarEstadoHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.ConsultarCategoriaService;
import com.rentaherramientas.application.catalog.usecase.ConsultarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.ConsultarHerramientaService;
import com.rentaherramientas.application.catalog.usecase.ConsultarHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.CrearCategoriaService;
import com.rentaherramientas.application.catalog.usecase.CrearCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.EliminarCategoriaService;
import com.rentaherramientas.application.catalog.usecase.EliminarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.GestionarImagenesHerramientaService;
import com.rentaherramientas.application.catalog.usecase.GestionarImagenesHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.RegistrarHerramientaService;
import com.rentaherramientas.application.catalog.usecase.RegistrarHerramientaUseCase;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los casos de uso de catalog. Igual que en identity, los *Service son
 * Java puro y aquí se instancian con sus puertos ya resueltos.
 */
@Configuration
public class CatalogBeansConfig {

    /** Bean del caso de uso de crear categorías. */
    @Bean
    CrearCategoriaUseCase crearCategoriaUseCase(CategoriaRepository categoriaRepository) {
        return new CrearCategoriaService(categoriaRepository);
    }

    /** Bean del caso de uso de actualizar categorías. */
    @Bean
    ActualizarCategoriaUseCase actualizarCategoriaUseCase(CategoriaRepository categoriaRepository) {
        return new ActualizarCategoriaService(categoriaRepository);
    }

    /** Bean del caso de uso de eliminar categorías. */
    @Bean
    EliminarCategoriaUseCase eliminarCategoriaUseCase(CategoriaRepository categoriaRepository) {
        return new EliminarCategoriaService(categoriaRepository);
    }

    /** Bean del caso de uso de consultar categorías. */
    @Bean
    ConsultarCategoriaUseCase consultarCategoriaUseCase(CategoriaRepository categoriaRepository) {
        return new ConsultarCategoriaService(categoriaRepository);
    }

    /** Bean del caso de uso de registrar herramientas. */
    @Bean
    RegistrarHerramientaUseCase registrarHerramientaUseCase(HerramientaRepository herramientaRepository,
                                                            CategoriaRepository categoriaRepository) {
        return new RegistrarHerramientaService(herramientaRepository, categoriaRepository);
    }

    /** Bean del caso de uso de actualizar herramientas. */
    @Bean
    ActualizarHerramientaUseCase actualizarHerramientaUseCase(HerramientaRepository herramientaRepository,
                                                              CategoriaRepository categoriaRepository) {
        return new ActualizarHerramientaService(herramientaRepository, categoriaRepository);
    }

    /** Bean del caso de uso de cambiar el estado o dar de baja herramientas. */
    @Bean
    CambiarEstadoHerramientaUseCase cambiarEstadoHerramientaUseCase(HerramientaRepository herramientaRepository) {
        return new CambiarEstadoHerramientaService(herramientaRepository);
    }

    /** Bean del caso de uso de consultar herramientas. */
    @Bean
    ConsultarHerramientaUseCase consultarHerramientaUseCase(HerramientaRepository herramientaRepository) {
        return new ConsultarHerramientaService(herramientaRepository);
    }

    /** Bean del caso de uso de gestionar la galería de imágenes. */
    @Bean
    GestionarImagenesHerramientaUseCase gestionarImagenesHerramientaUseCase(
            HerramientaRepository herramientaRepository, AlmacenamientoImagenes almacenamientoImagenes) {
        return new GestionarImagenesHerramientaService(herramientaRepository, almacenamientoImagenes);
    }
}
