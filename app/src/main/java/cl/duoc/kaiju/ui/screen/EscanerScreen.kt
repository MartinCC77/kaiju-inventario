package cl.duoc.kaiju.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Pantalla que usa la cámara para leer un código QR o de barras.
 * - Pide el permiso de cámara solo cuando se abre (no al iniciar la app).
 * - Si el usuario lo rechaza, muestra un mensaje y permite volver.
 * - No guarda fotos ni imágenes: solo lee el texto del código.
 *
 * @param onCodigoDetectado Se llama con el texto del código leído.
 * @param onCerrar Se llama al pulsar volver.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerScreen(
    onCodigoDetectado: (String) -> Unit,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current

    // ¿Ya tenemos el permiso de cámara?
    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var permisoRechazado by remember { mutableStateOf(false) }

    // Lanzador que muestra el cuadro del sistema para pedir el permiso
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        permisoRechazado = !concedido
    }

    // Al abrir la pantalla, si falta el permiso, se pide
    LaunchedEffect(Unit) {
        if (!permisoConcedido) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    // El botón "atrás" del celular cierra el escáner
    BackHandler { onCerrar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear código") },
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (permisoConcedido) {
                VistaCamara(onCodigoDetectado)
            } else {
                // Sin permiso: se explica qué pasa y se ofrecen opciones
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text(
                        text = if (permisoRechazado)
                            "Sin el permiso de cámara no se puede escanear. " +
                                    "Puedes escribir el código manualmente."
                        else
                            "Se necesita el permiso de cámara para escanear códigos.",
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) }) {
                        Text("Dar permiso")
                    }
                    OutlinedButton(onClick = onCerrar) { Text("Volver") }
                }
            }
        }
    }
}

/** Muestra la cámara y detecta códigos con ML Kit. Solo se usa cuando hay permiso. */
@Composable
private fun VistaCamara(onCodigoDetectado: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    // Controlador de CameraX: maneja la vista previa y el análisis de imágenes
    val controlador = remember { LifecycleCameraController(context) }
    // Evita avisar muchas veces el mismo código
    var yaDetectado by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        // ML Kit: solo busca QR y los códigos de barras más comunes
        val opciones = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39
            )
            .build()
        val escaner = BarcodeScanning.getClient(opciones)
        val ejecutor = ContextCompat.getMainExecutor(context)

        // Cada imagen de la cámara pasa por ML Kit; si encuentra un código, lo entrega
        controlador.setImageAnalysisAnalyzer(
            ejecutor,
            MlKitAnalyzer(
                listOf(escaner),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ejecutor
            ) { resultado ->
                val codigo = resultado?.getValue(escaner)?.firstOrNull()?.rawValue
                if (codigo != null && !yaDetectado) {
                    yaDetectado = true
                    onCodigoDetectado(codigo)
                }
            }
        )
        controlador.bindToLifecycle(lifecycleOwner)

        // Al salir de la pantalla se libera la cámara
        onDispose {
            controlador.unbind()
            escaner.close()
        }
    }

    // Animación: el marco del visor parpadea suavemente
    val transicion = rememberInfiniteTransition(label = "marcoVisor")
    val alfa by transicion.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alfaMarco"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Vista previa de la cámara
        AndroidView(
            factory = { ctx -> PreviewView(ctx).apply { controller = controlador } },
            modifier = Modifier.fillMaxSize()
        )
        // Marco guía para apuntar al código
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.Center)
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alfa),
                    shape = RoundedCornerShape(16.dp)
                )
        )
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Text(
                "Apunta la cámara al código QR o de barras",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}