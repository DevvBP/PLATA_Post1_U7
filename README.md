# Gestión de Tareas — Laboratorio Unidad 7

**Asignatura:** Programación Web  
**Estudiante:** Brayan Plata  
**Correo:** 02230132025@mail.udes.edu.co  
**Repositorio:** [DevvBP/PLATA_Post1_U7](https://github.com/DevvBP/PLATA_Post1_U7)

---

## Descripción

Este proyecto implementa el laboratorio de la **Unidad 7: Gestión de Tareas con Spring Boot**, el cual integra en un único proyecto dos capas de presentación que comparten la misma lógica de negocio:

| Capa | Tecnología | Ruta base |
|------|-----------|-----------|
| Vista web (MVC) | Spring MVC + Thymeleaf | `/tareas` |
| API RESTful | Spring REST | `/api/tareas` |

La aplicación permite crear, editar, filtrar, completar y eliminar tareas, con validación de datos mediante Jakarta Bean Validation en ambas capas.

---

## Estructura del proyecto

```
src/main/java/com/universidad/tareas/
├── TareasApplication.java              ← Punto de entrada Spring Boot
├── model/
│   ├── Prioridad.java                  ← Enum (ALTA, MEDIA, BAJA)
│   └── Tarea.java                      ← Entidad con Bean Validation
├── service/
│   └── TareaService.java               ← Lógica de negocio (almacenamiento en memoria)
└── controller/
    ├── TareaController.java            ← Controlador MVC @Controller
    ├── TareaApiController.java         ← Controlador REST @RestController
    └── ApiErrorHandler.java            ← Manejo global de errores (@RestControllerAdvice)

src/main/resources/
├── application.properties
└── templates/tareas/
    ├── lista.html                      ← Vista principal con filtros
    └── formulario.html                 ← Formulario de creación/edición
```

---

## Requisitos previos

- **Java 17** o superior
- **Maven 3.6+**
- Conexión a Internet (primera compilación descarga dependencias)

---

## Compilación y ejecución

### Iniciar la aplicación

```bash
mvn spring-boot:run
```

La aplicación queda disponible en: **http://localhost:8080**

### Vista web Thymeleaf

```
http://localhost:8080/tareas
```

### Compilar el artefacto JAR

```bash
mvn clean package
java -jar target/tareas-1.0.0.jar
```

---

## Endpoints de la API REST

Base URL: `http://localhost:8080/api/tareas`

| Método | Ruta | Descripción | Código éxito | Código error |
|--------|------|-------------|:------------:|:------------:|
| `GET` | `/api/tareas` | Listar todas las tareas (filtros opcionales: `?prioridad=ALTA&completada=false`) | 200 | — |
| `GET` | `/api/tareas/{id}` | Obtener una tarea por ID | 200 | 404 |
| `POST` | `/api/tareas` | Crear nueva tarea (JSON en body, devuelve header `Location`) | 201 | 400 |
| `PUT` | `/api/tareas/{id}` | Reemplazar tarea completa | 200 | 404 |
| `PATCH` | `/api/tareas/{id}/completar` | Marcar tarea como completada (modificación parcial) | 200 | 404 |
| `DELETE` | `/api/tareas/{id}` | Eliminar tarea | 204 | 404 |

### Ejemplo de cuerpo JSON para POST/PUT

```json
{
  "titulo": "Revisar documentación de API",
  "descripcion": "Leer y actualizar los contratos Swagger del módulo de pagos.",
  "prioridad": "ALTA",
  "fechaLimite": "2026-12-31"
}
```

### Respuesta de error de validación (HTTP 400)

```json
{
  "titulo": "El título debe tener entre 3 y 100 caracteres",
  "fechaLimite": "La fecha límite debe ser hoy o una fecha futura"
}
```

---

## Capturas de pantalla

Las capturas deben ser almacenadas en la carpeta `capturas/` con los siguientes nombres:

| Archivo | Descripción |
|---------|-------------|
| `lista-tareas.png` | Vista `/tareas` con filtros aplicados |
| `formulario-error.png` | Formulario mostrando errores de validación |
| `postman-post-201.png` | POST a `/api/tareas` con JSON válido → 201 Created |
| `postman-post-400.png` | POST a `/api/tareas` con JSON inválido → 400 Bad Request |

---

## Decisiones de diseño

### 1. Inyección por constructor en vez de `@Autowired` en campo

Los controladores y servicios reciben sus dependencias exclusivamente a través del constructor, sin usar `@Autowired` como anotación de campo. Esto garantiza que el objeto siempre esté en un estado válido al ser instanciado, hace las dependencias explícitas en la firma del constructor y facilita significativamente las pruebas unitarias (se puede inyectar un mock directamente sin necesidad de un contenedor de Spring).

### 2. `@FutureOrPresent` en vez de `@Future` para `fechaLimite`

Se eligió `@FutureOrPresent` en lugar de `@Future` en el campo `fechaLimite` de la entidad `Tarea`. La razón es semántica: una tarea puede tener fecha límite para el día de hoy mismo (trabajo urgente del día), algo perfectamente válido en un sistema de gestión de tareas. `@Future` rechazaría esa fecha, mientras que `@FutureOrPresent` la acepta correctamente.

### 3. `POST` en vez de `GET` para completar/eliminar en MVC

Las acciones de completar y eliminar tareas desde la vista Thymeleaf se procesan mediante formularios con `method="post"`. Usar un enlace `<a href>` para estas operaciones sería un error de diseño: los navegadores pueden hacer prefetch de enlaces, los rastreadores web los siguen, y el botón de volver recarga el formulario. El estándar HTTP reserva `GET` para operaciones seguras e idempotentes que no modifican el estado del servidor.

### 4. `PATCH` en vez de `PUT` para completar una tarea en la API REST

En la API REST, marcar una tarea como completada utiliza `PATCH /api/tareas/{id}/completar` en lugar de `PUT`. Semánticamente, `PUT` implica el reemplazo completo del recurso con el cuerpo enviado, mientras que `PATCH` representa una modificación parcial. Dado que solo se actualiza el campo `completada`, `PATCH` es el verbo HTTP correcto y más preciso para esta operación.

### 5. Manejo separado de validación: `BindingResult` para HTML vs `@RestControllerAdvice` para JSON

Cada capa de presentación responde en su formato nativo. En el controlador MVC (`TareaController`), los errores de validación se capturan con `BindingResult` y se reinyectan al modelo para que Thymeleaf los renderice en el HTML. En la capa REST, el `ApiErrorHandler` con `@RestControllerAdvice` intercepta la excepción `MethodArgumentNotValidException` y la transforma en un `Map<String, String>` serializado como JSON con HTTP 400. Mezclar ambos enfoques en un mismo manejador global generaría respuestas inconsistentes.

### 6. `Map` en memoria en lugar de JPA/base de datos

El almacenamiento de tareas se implementa con un `LinkedHashMap<Long, Tarea>` en la clase `TareaService`. Esta decisión es deliberada y responde al alcance de la Unidad 7, que se enfoca en el manejo de peticiones HTTP, validación y capas de presentación. La persistencia real con JPA e Hibernate se introducirá en la Unidad 8, donde se migrará esta implementación a un repositorio `JpaRepository` sin cambios en los controladores.

---

## Tecnologías utilizadas

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 3.3.4 | Framework base |
| Spring MVC | — | Capa de presentación web |
| Thymeleaf | — | Motor de plantillas HTML |
| Jakarta Bean Validation | — | Validación de datos |
| Spring DevTools | — | Recarga automática en desarrollo |
| Maven | 3.6+ | Gestión de dependencias y compilación |