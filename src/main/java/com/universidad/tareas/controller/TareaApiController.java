package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de tareas.
 * Comparte la misma instancia de {@link TareaService} que el controlador MVC,
 * demostrando que la capa de servicio es agnóstica a la capa de presentación.
 *
 * <p>Endpoints disponibles:</p>
 * <ul>
 *   <li>GET    /api/tareas          — listar con filtros opcionales</li>
 *   <li>GET    /api/tareas/{id}      — obtener por ID (200 / 404)</li>
 *   <li>POST   /api/tareas          — crear nueva tarea (201 / 400)</li>
 *   <li>PUT    /api/tareas/{id}      — reemplazar tarea completa (200 / 404)</li>
 *   <li>PATCH  /api/tareas/{id}/completar — marcar completada (200 / 404)</li>
 *   <li>DELETE /api/tareas/{id}      — eliminar tarea (204 / 404)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tareas")
public class TareaApiController {

    private final TareaService tareaService;

    public TareaApiController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    // ── GET /api/tareas ──────────────────────────────────────────────────────

    /**
     * Lista todas las tareas. Admite filtrado opcional por prioridad y estado.
     *
     * @param prioridad filtro opcional de prioridad
     * @param completada filtro opcional de estado
     * @return 200 OK con la lista de tareas (puede estar vacía)
     */
    @GetMapping
    public ResponseEntity<List<Tarea>> listar(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada) {

        return ResponseEntity.ok(tareaService.filtrar(prioridad, completada));
    }

    // ── GET /api/tareas/{id} ─────────────────────────────────────────────────

    /**
     * Obtiene una tarea por su ID.
     *
     * @param id identificador de la tarea
     * @return 200 OK con la tarea, o 404 Not Found si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerPorId(@PathVariable Long id) {
        return tareaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /api/tareas ─────────────────────────────────────────────────────

    /**
     * Crea una nueva tarea.
     * La respuesta incluye el encabezado {@code Location} apuntando al recurso creado.
     *
     * @param tarea objeto de tarea a crear (validado automáticamente)
     * @return 201 Created con la tarea creada, o 400 Bad Request si hay errores de validación
     */
    @PostMapping
    public ResponseEntity<Tarea> crear(@Valid @RequestBody Tarea tarea) {
        tarea.setId(null); // garantiza que siempre se genere un ID nuevo
        Tarea creada = tareaService.guardar(tarea);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    // ── PUT /api/tareas/{id} ─────────────────────────────────────────────────

    /**
     * Reemplaza completamente una tarea existente.
     * Semántica PUT: el cuerpo de la petición representa el estado completo del recurso.
     *
     * @param id identificador de la tarea a reemplazar
     * @param tarea nuevo estado completo de la tarea
     * @return 200 OK con la tarea actualizada, o 404 Not Found si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> reemplazar(@PathVariable Long id,
                                            @Valid @RequestBody Tarea tarea) {
        return tareaService.buscarPorId(id)
                .map(existente -> {
                    tarea.setId(id);
                    return ResponseEntity.ok(tareaService.guardar(tarea));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── PATCH /api/tareas/{id}/completar ─────────────────────────────────────

    /**
     * Modifica parcialmente una tarea marcándola como completada.
     * Se usa PATCH en lugar de PUT porque solo se actualiza un campo (estado),
     * no el recurso completo.
     *
     * @param id identificador de la tarea
     * @return 200 OK con la tarea actualizada, o 404 Not Found si no existe
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<Tarea> completar(@PathVariable Long id) {
        if (tareaService.marcarCompletada(id)) {
            return tareaService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.notFound().build();
    }

    // ── DELETE /api/tareas/{id} ──────────────────────────────────────────────

    /**
     * Elimina una tarea por su ID.
     *
     * @param id identificador de la tarea a eliminar
     * @return 204 No Content si fue eliminada, o 404 Not Found si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (tareaService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
