package cl.duoc.kaiju.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cl.duoc.kaiju.ui.components.CampoTexto
import cl.duoc.kaiju.ui.components.VentaItem
import cl.duoc.kaiju.viewmodel.VentaViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.QrCodeScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    viewModel: VentaViewModel = viewModel()
) {
    val estado by viewModel.state.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val ventas by viewModel.ventas.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Controla si el menú desplegable de productos está abierto
    var menuAbierto by remember { mutableStateOf(false) }
    val productoSeleccionado = productos.find { it.id == estado.productoId }

    // Controla si se muestra el escáner de la cámara
    var mostrarEscaner by remember { mutableStateOf(false) }

    if (mostrarEscaner) {
        EscanerScreen(
            onCodigoDetectado = { codigo ->
                mostrarEscaner = false
                // Busca el producto cuyo código coincide con el leído
                val encontrado = productos.find {
                    it.codigo.equals(codigo.trim(), ignoreCase = true)
                }
                if (encontrado != null) {
                    viewModel.onProductoSeleccionado(encontrado)
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("No existe un producto con el código $codigo")
                    }
                }
            },
            onCerrar = { mostrarEscaner = false }
        )
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ventas") }) },
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
                Text("Registrar venta", style = MaterialTheme.typography.titleLarge)
            }

            if (productos.isEmpty()) {
                item {
                    Text(
                        "Primero agrega productos en la pestaña Productos.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { mostrarEscaner = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Text("  Escanear producto con la cámara")
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
                                    viewModel.onProductoSeleccionado(producto)
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

            // Datos del producto elegido
            productoSeleccionado?.let { producto ->
                item {
                    Text(
                        "Precio: $${"%.0f".format(producto.precio)} · Stock disponible: ${producto.stock}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                CampoTexto(
                    valor = estado.cantidad,
                    etiqueta = "Cantidad a vender",
                    icono = Icons.Filled.Tag,
                    error = estado.errores.cantidad,
                    onChange = viewModel::onCantidadChanged,
                    tipoTeclado = KeyboardType.Number
                )
            }

            // Total de la venta
            item {
                Text(
                    "Total: $${"%.0f".format(estado.total)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Button(
                    onClick = {
                        viewModel.registrarVenta {
                            scope.launch { snackbarHostState.showSnackbar("Venta registrada") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar venta")
                }
            }

            item {
                Text("Historial (${ventas.size})", style = MaterialTheme.typography.titleLarge)
            }
            items(ventas, key = { it.id }) { venta ->
                Box(modifier = Modifier.animateItem()) {
                    VentaItem(venta)
                }
            }
        }
    }
}