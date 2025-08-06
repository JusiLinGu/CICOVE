package com.example.cicove.notificaciones
/*
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cicove.R

class RecibirNotificacion : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nombreMascota = intent.getStringExtra("nombreMascota") ?: "tu mascota"
        val hora = intent.getStringExtra("hora") ?: "hora desconocida"

        val builder = NotificationCompat.Builder(context, "CITA_CHANNEL")
            .setSmallIcon(R.drawable.img) // Usa un ícono real, no transparente
            .setContentTitle("Cita Veterinaria")
            .setContentText("Tu cita con $nombreMascota es a las $hora.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // El permiso no está otorgado, así que no se lanza la notificación.
            return
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
*/