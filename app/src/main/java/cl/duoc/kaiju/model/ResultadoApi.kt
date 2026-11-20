package cl.duoc.kaiju.model

// Resultado de una llamada al backend: salió bien (con datos) o salió mal (con un mensaje)
sealed interface ResultadoApi<out T> {
    data class Exito<out T>(val datos: T) : ResultadoApi<T>
    data class Error(val mensaje: String) : ResultadoApi<Nothing>
}