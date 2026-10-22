package cl.duoc.kaiju.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.kaiju.MainActivity


object NotificacionHelper {

    private const val CANAL_ID = "stock_bajo"

    fun tienePermiso(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Alertas de stock bajo",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisa cuando un producto llega a su stock mínimo"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(canal)
    }

    @SuppressLint("MissingPermission") // el permiso se revisa en la primera línea
    fun mostrarStockBajo(
        context: Context,
        productoId: Long,
        nombre: String,
        stock: Int,
        stockMinimo: Int
    ) {
        if (!tienePermiso(context)) return

        crearCanal(context)

        // Al tocar la notificación se abre la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Stock bajo: $nombre")
            .setContentText("Quedan $stock unidades (mínimo $stockMinimo). Conviene reponer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Se usa el id del producto: si vuelve a bajar, actualiza el aviso en vez de repetirlo
        NotificationManagerCompat.from(context).notify(productoId.toInt(), notificacion)
    }
}