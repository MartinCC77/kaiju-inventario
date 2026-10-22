package cl.duoc.kaiju.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import cl.duoc.kaiju.ui.components.ProductoItem
import cl.duoc.kaiju.viewmodel.ProductoViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel = viewModel(),
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.state.collectAsState()
    val productos by viewModel.productos.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Controla si se muestra el escáner de la cámara
    var mostrarEscaner by remember { mutableStateOf(false) }

    // Si el escáner está abierto, se muestra en vez del formulario
    if (mostrarEscaner) {
        EscanerScreen(
            onCodigoDetectado = { codigo ->
                viewModel.onCodigoChanged(codigo.trim()) // rellena el campo Código
                mostrarEscaner = false
            },
            onCerrar = { mostrarEscaner = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaiju · Inventario") },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
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
                Text("Nuevo producto", style = MaterialTheme.typography.titleLarge)
            }
            item {
                OutlinedButton(
                    onClick = { mostrarEscaner = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Text("  Escanear código con la cámara")
                }
            }
            item {
                CampoTexto(
                    valor = estado.codigo,
                    etiqueta = "Código",
                    icono = Icons.Filled.Tag,
                    error = estado.errores.codigo,
                    onChange = viewModel::onCodigoChanged
                )
            }
            item {
                CampoTexto(
                    valor = estado.nombre,
                    etiqueta = "Nombre",
                    icono = Icons.Filled.Inventory2,
                    error = estado.errores.nombre,
                    onChange = viewModel::onNombreChanged
                )
            }
            item {
                CampoTexto(
                    valor = estado.descripcion,
                    etiqueta = "Descripción (opcional)",
                    icono = Icons.Filled.Description,
                    error = null,
                    onChange = viewModel::onDescripcionChanged
                )
            }
            item {
                CampoTexto(
                    valor = estado.categoria,
                    etiqueta = "Categoría",
                    icono = Icons.Filled.Category,
                    error = estado.errores.categoria,
                    onChange = viewModel::onCategoriaChanged
                )
            }
            item {
                CampoTexto(
                    valor = estado.precio,
                    etiqueta = "Precio",
                    icono = Icons.Filled.AttachMoney,
                    error = estado.errores.precio,
                    onChange = viewModel::onPrecioChanged,
                    tipoTeclado = KeyboardType.Decimal
                )
            }
            item {
                CampoTexto(
                    valor = estado.stock,
                    etiqueta = "Stock actual",
                    icono = Icons.Filled.Inventory2,
                    error = estado.errores.stock,
                    onChange = viewModel::onStockChanged,
                    tipoTeclado = KeyboardType.Number
                )
            }
            item {
                CampoTexto(
                    valor = estado.stockMinimo,
                    etiqueta = "Stock mínimo",
                    icono = Icons.Filled.Warning,
                    error = estado.errores.stockMinimo,
                    onChange = viewModel::onStockMinimoChanged,
                    tipoTeclado = KeyboardType.Number
                )
            }
            item {
                Button(
                    onClick = {
                        viewModel.guardarProducto {
                            scope.launch { snackbarHostState.showSnackbar("Producto guardado") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar producto")
                }
            }

            item {
                Text("Inventario (${productos.size})", style = MaterialTheme.typography.titleLarge)
            }
            items(productos, key = { it.id }) { producto ->
                // Box con animateItem: la tarjeta se desliza al agregar o eliminar productos
                Box(modifier = Modifier.animateItem()) {
                    ProductoItem(
                        producto = producto,
                        onEliminar = { viewModel.eliminarProducto(producto) }
                    )
                }
            }
        }
    }
}
