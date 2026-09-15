package com.universidad.tareas.service;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Capa de servicio para la gestión de tareas en memoria.
 * Utiliza un {@link LinkedHashMap} para preservar el orden de inserción.
 * En la Unidad 8 esta implementación será reemplazada por una capa JPA/Hibernate.
 */
@Service
public class TareaService {

    private final Map<Long, Tarea> almacenamiento = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    /**
     * Constructor que pre-carga tres tareas de ejemplo al iniciar la aplicación.
     */
    public TareaService() {
        guardar(new Tarea(null,
                "Preparar informe mensual",
                "Consolidar métricas y redactar resumen ejecutivo para la reunión de dirección.",
                Prioridad.ALTA,
                LocalDate.now().plusDays(3)));

        guardar(new Tarea(null,
                "Revisar solicitudes de soporte",
                "Atender y clasificar los tickets abiertos del mes en el sistema de helpdesk.",
                Prioridad.MEDIA,
                LocalDate.now().plusDays(7)));

        guardar(new Tarea(null,
                "Actualizar documentación técnica",
                "Incorporar los cambios del último sprint en la wiki del equipo de desarrollo.",
                Prioridad.BAJA,
                LocalDate.now().plusDays(14)));
    }

    // ── Consultas ────────────────────────────────────────────────────────────

    /**
     * Devuelve todas las tareas almacenadas en orden de inserción.
     */
    public List<Tarea> obtenerTodas() {
        return new ArrayList<>(almacenamiento.values());
    }

    /**
     * Filtra las tareas por prioridad y/o estado de completitud.
     * Los parámetros nulos se ignoran (sin filtro aplicado para ese campo).
     *
     * @param prioridad prioridad deseada, o {@code null} para no filtrar
     * @param completada estado deseado, o {@code null} para no filtrar
     * @return lista filtrada de tareas
     */
    public List<Tarea> filtrar(Prioridad prioridad, Boolean completada) {
        return almacenamiento.values().stream()
                .filter(t -> prioridad == null || t.getPrioridad() == prioridad)
                .filter(t -> completada == null || t.isCompletada() == completada)
                .collect(Collectors.toList());
    }

    /**
     * Busca una tarea por su identificador único.
     *
     * @param id identificador de la tarea
     * @return un {@link Optional} con la tarea encontrada, o vacío si no existe
     */
    public Optional<Tarea> buscarPorId(Long id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    // ── Mutaciones ───────────────────────────────────────────────────────────

    /**
     * Persiste una tarea nueva o actualiza una existente.
     * Si la tarea no tiene ID asignado, se genera uno nuevo automáticamente.
     *
     * @param tarea objeto a guardar
     * @return la tarea con el ID asignado
     */
    public Tarea guardar(Tarea tarea) {
        if (tarea.getId() == null) {
            tarea.setId(secuencia.getAndIncrement());
        }
        almacenamiento.put(tarea.getId(), tarea);
        return tarea;
    }

    /**
     * Marca una tarea como completada.
     *
     * @param id identificador de la tarea
     * @return {@code true} si la tarea existía y fue actualizada, {@code false} en caso contrario
     */
    public boolean marcarCompletada(Long id) {
        Tarea tarea = almacenamiento.get(id);
        if (tarea == null) {
            return false;
        }
        tarea.setCompletada(true);
        return true;
    }

    /**
     * Elimina una tarea del almacenamiento.
     *
     * @param id identificador de la tarea a eliminar
     * @return {@code true} si la tarea existía y fue eliminada, {@code false} en caso contrario
     */
    public boolean eliminar(Long id) {
        return almacenamiento.remove(id) != null;
    }
}
