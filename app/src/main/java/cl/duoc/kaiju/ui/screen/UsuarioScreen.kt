package cl.duoc.kaiju.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.kaiju.data.remote.UsuarioResponse
import cl.duoc.kaiju.ui.components.CampoTexto
import cl.duoc.kaiju.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioScreen(
    viewModel: UsuarioViewModel = viewModel(factory = UsuarioViewModel.Factory),
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaiju · Usuarios") },
                actions = {
                    IconButton(onClick = viewModel::cargarUsuarios) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar lista")
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::abrirFormularioNuevo) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo usuario")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Barra de carga mientras se consulta al servidor
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Error general (sin conexión, etc.) con botón para reintentar
            estado.mensaje?.let { mensaje ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::cargarUsuarios) { Text("Reintentar") }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Usuarios (${estado.usuarios.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(estado.usuarios, key = { it.id }) { usuario ->
                    // Box con animateItem: la tarjeta se desliza al agregar o eliminar usuarios
                    Box(modifier = Modifier.animateItem()) {
                        UsuarioItem(
                            usuario = usuario,
                            onEditar = { viewModel.abrirFormularioEditar(usuario) },
                            onEliminar = { viewModel.pedirEliminar(usuario) }
                        )
                    }
                }
            }
        }
    }

    // Cuadro para crear o editar un usuario
    if (estado.formularioVisible) {
        FormularioUsuario(estado = estado, viewModel = viewModel)
    }

    // Cuadro de confirmación antes de eliminar
    estado.usuarioAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminar,
            title = { Text("Eliminar usuario") },
            text = { Text("¿Seguro que quieres eliminar a ${usuario.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = viewModel::confirmarEliminar) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminar) { Text("Cancelar") }
            }
        )
    }
}

// Tarjeta de un usuario de la lista
@Composable
private fun UsuarioItem(
    usuario: UsuarioResponse,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                Text(usuario.correo, style = MaterialTheme.typography.bodyMedium)
                Text("Rol: ${usuario.rol}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar usuario")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar usuario")
            }
        }
    }
}

// Formulario (en un cuadro) para crear o editar un usuario
@Composable
private fun FormularioUsuario(
    estado: cl.duoc.kaiju.model.UsuarioUiState,
    viewModel: UsuarioViewModel
) {
    val editando = estado.editandoId != null

    AlertDialog(
        onDismissRequest = viewModel::cerrarFormulario,
        title = { Text(if (editando) "Editar usuario" else "Nuevo usuario") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto(
                    valor = estado.nombre,
                    etiqueta = "Nombre",
                    icono = Icons.Filled.Person,
                    error = estado.errores.nombre,
                    onChange = viewModel::onNombreChanged
                )
                CampoTexto(
                    valor = estado.correo,
                    etiqueta = "Correo",
                    icono = Icons.Filled.Email,
                    error = estado.errores.correo,
                    onChange = viewModel::onCorreoChanged,
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = estado.contrasena,
                    etiqueta = if (editando) "Contraseña nueva (opcional)" else "Contraseña",
                    icono = Icons.Filled.Lock,
                    error = estado.errores.contrasena,
                    onChange = viewModel::onContrasenaChanged,
                    tipoTeclado = KeyboardType.Password,
                    esClave = true
                )

                // Selector del rol
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("USUARIO", "ADMIN").forEach { rol ->
                        FilterChip(
                            selected = estado.rol == rol,
                            onClick = { viewModel.onRolChanged(rol) },
                            label = { Text(rol) }
                        )
                    }
                }

                // Error devuelto por el servidor (por ejemplo, correo ya registrado)
                estado.errores.general?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::guardar, enabled = !estado.guardando) {
                Text(if (estado.guardando) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cerrarFormulario) { Text("Cancelar") }
        }
    )
}