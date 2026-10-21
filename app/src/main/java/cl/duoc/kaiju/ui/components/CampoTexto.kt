package cl.duoc.kaiju.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation


@Composable
fun CampoTexto(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    onChange: (String) -> Unit,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}