package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para la gestión de tareas mediante vistas Thymeleaf.
 * Implementa el patrón PRG (Post-Redirect-Get) en todas las operaciones de escritura
 * para evitar el reenvío accidental de formularios al recargar la página.
 *
 * <p>La inyección de dependencias se realiza por constructor para garantizar
 * la inmutabilidad del servicio y facilitar las pruebas unitarias.</p>
 */
@Controller
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    // ── GET /tareas ──────────────────────────────────────────────────────────

    /**
     * Lista todas las tareas, con filtrado opcional por prioridad y estado.
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada,
            Model model) {

        model.addAttribute("tareas", tareaService.filtrar(prioridad, completada));
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("filtroPrioridad", prioridad);
        model.addAttribute("filtroCompletada", completada);
        return "tareas/lista";
    }

    // ── GET /tareas/nueva ────────────────────────────────────────────────────

    /**
     * Muestra el formulario para crear una tarea nueva.
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("tarea", new Tarea());
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("modoEdicion", false);
        return "tareas/formulario";
    }

    // ── GET /tareas/{id}/editar ──────────────────────────────────────────────

    /**
     * Muestra el formulario precargado con los datos de una tarea existente.
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return tareaService.buscarPorId(id)
                .map(tarea -> {
                    model.addAttribute("tarea", tarea);
                    model.addAttribute("prioridades", Prioridad.values());
                    model.addAttribute("modoEdicion", true);
                    return "tareas/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("mensajeError",
                            "No se encontró la tarea con ID " + id);
                    return "redirect:/tareas";
                });
    }

    // ── POST /tareas/guardar ─────────────────────────────────────────────────

    /**
     * Procesa el formulario de creación o edición de una tarea.
     * Si la validación falla, regresa al formulario con los errores detallados.
     * Si tiene éxito, redirige al listado aplicando el patrón PRG.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid Tarea tarea, BindingResult resultado,
                          Model model, RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            model.addAttribute("prioridades", Prioridad.values());
            model.addAttribute("modoEdicion", tarea.getId() != null);
            return "tareas/formulario";
        }

        tareaService.guardar(tarea);
        String mensaje = (tarea.getId() == null)
                ? "Tarea creada correctamente."
                : "Tarea actualizada correctamente.";
        redirectAttributes.addFlashAttribute("mensajeExito", mensaje);
        return "redirect:/tareas";
    }

    // ── POST /tareas/{id}/completar ──────────────────────────────────────────

    /**
     * Marca una tarea como completada.
     * Se usa POST para evitar que crawlers o prefetch del navegador ejecuten
     * acciones destructivas o con efectos secundarios mediante GET.
     */
    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (tareaService.marcarCompletada(id)) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Tarea marcada como completada.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se encontró la tarea con ID " + id);
        }
        return "redirect:/tareas";
    }

    // ── POST /tareas/{id}/eliminar ───────────────────────────────────────────

    /**
     * Elimina una tarea del sistema.
     * Se usa POST (no DELETE/GET) para proteger la operación de efectos secundarios
     * no intencionales desde navegadores o agentes automatizados.
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (tareaService.eliminar(id)) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Tarea eliminada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se encontró la tarea con ID " + id);
        }
        return "redirect:/tareas";
    }
}
