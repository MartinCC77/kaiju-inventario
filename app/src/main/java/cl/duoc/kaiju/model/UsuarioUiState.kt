package cl.duoc.kaiju.model

import cl.duoc.kaiju.data.remote.UsuarioResponse

data class UsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val contrasena: String? = null,
    val general: String? = null // error devuelto por el servidor (por ejemplo, correo repetido)
)

data class UsuarioUiState(
    val usuarios: List<UsuarioResponse> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null, // error general al cargar o eliminar (sin conexión, etc.)

    // Formulario de crear / editar
    val formularioVisible: Boolean = false,
    val editandoId: Long? = null, // null = se está creando un usuario nuevo
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val rol: String = "USUARIO",
    val errores: UsuarioErrores = UsuarioErrores(),

    // Usuario que se está por eliminar (se muestra un cuadro de confirmación)
    val usuarioAEliminar: UsuarioResponse? = null
)