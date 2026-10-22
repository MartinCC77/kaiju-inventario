package cl.duoc.kaiju.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.kaiju.model.TipoMovimiento
import cl.duoc.kaiju.ui.components.CampoTexto
import cl.duoc.kaiju.ui.components.MovimientoItem
import cl.duoc.kaiju.viewmodel.MovimientoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(
    viewModel: MovimientoViewModel = viewModel()
) {
    val estado by viewModel.state.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val movimientos by viewModel.movimientos.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Controla si el menú desplegable de productos está abierto
    var menuAbierto by remember { mutableStateOf(false) }
    val productoSeleccionado = productos.find { it.id == estado.productoId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Movimientos de stock") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Registrar movimiento", style = MaterialTheme.typography.titleLarge)
            }

            if (productos.isEmpty()) {
                item {
                    Text(
                        "Primero agrega productos en la pestaña Productos.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Selector de producto
            item {
                Box {
                    OutlinedButton(
                        onClick = { menuAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Inventory2, contentDescription = null)
                        Text(
                            text = productoSeleccionado?.let { "${it.codigo} · ${it.nombre}" }
                                ?: "Seleccionar producto",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        productos.forEach { producto ->
                            DropdownMenuItem(
                                text = {
                                    Text("${producto.codigo} · ${producto.nombre} (stock ${producto.stock})")
                                },
                                onClick = {
                                    viewModel.onProductoSeleccionado(producto.id)
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }
            }
            estado.errores.producto?.let { mensaje ->
                item {
                    Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Selector de tipo de movimiento
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoMovimiento.entries.forEach { tipo ->
                        FilterChip(
                            selected = estado.tipo == tipo,
                            onClick = { viewModel.onTipoChanged(tipo) },
                            label = { Text(tipo.etiqueta) }
                        )
                    }
                }
            }

            item {
                CampoTexto(
                    valor = estado.cantidad,
                    etiqueta = if (estado.tipo == TipoMovimiento.AJUSTE) "Nuevo stock" else "Cantidad",
                    icono = Icons.Filled.Tag,
                    error = estado.errores.cantidad,
                    onChange = viewModel::onCantidadChanged,
                    tipoTeclado = KeyboardType.Number
                )
            }
            item {
                CampoTexto(
                    valor = estado.motivo,
                    etiqueta = if (estado.tipo == TipoMovimiento.AJUSTE) "Motivo (obligatorio)" else "Motivo (opcional)",
                    icono = Icons.Filled.Description,
                    error = estado.errores.motivo,
                    onChange = viewModel::onMotivoChanged
                )
            }
            item {
                Button(
                    onClick = {
                        viewModel.registrarMovimiento {
                            scope.launch { snackbarHostState.showSnackbar("Movimiento registrado") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar movimiento")
                }
            }

            item {
                Text("Historial (${movimientos.size})", style = MaterialTheme.typography.titleLarge)
            }
            items(movimientos, key = { it.id }) { movimiento ->
                Box(modifier = Modifier.animateItem()) {
                    MovimientoItem(movimiento)
                }
            }
        }
    }
}