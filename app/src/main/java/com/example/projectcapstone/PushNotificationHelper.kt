package com.example.projectcapstone

import android.Manifest
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
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

object PushNotificationHelper {
    private const val CHANNEL_ID = "integra_agency_push"
    private const val CHANNEL_NAME = "Integra Agency Notifications"
    private const val CHANNEL_DESC = "Real-time project status, commission, and broadcast notifications"
    private const val PREFS_NAME = "integra_push_prefs"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /** Tidak pernah menggunakan token buatan. Registrasi ke backend hanya token FCM asli. */
    fun obtainDeviceFcmToken(context: Context, onToken: (String) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcmToken ->
                if (fcmToken.isNotBlank()) {
                    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString("fcm_token", fcmToken).apply()
                    Log.d("IntegraFCM", "FCM registration token available (token value is not logged)")
                    onToken(fcmToken)
                }
            }
            .addOnFailureListener { error ->
                Log.e("IntegraFCM", "Failed to retrieve FCM token: ${error.message}")
            }
    }

    fun deleteLocalFcmToken(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove("fcm_token").apply()
        FirebaseMessaging.getInstance().deleteToken()
            .addOnFailureListener { error -> Log.w("IntegraFCM", "Failed to remove local token: ${error.message}") }
    }

    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_integra_notification)
            .setContentTitle(title.ifBlank { "PT Integra Cipta Data" })
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    /**
     * Checks the list of notifications from `/api/notifications` and displays an Android OS
     * status-bar push notification for any unread notification that has not been popped up yet.
     */
    fun dispatchUnreadNotifications(
        context: Context,
        roleOrUserKey: String,
        notifications: List<NotificationDto>
    ) {
        // Jika Firebase aktif, FCM yang mengatur status-bar notification.
        // Polling tetap dapat dipakai untuk memperbarui daftar notifikasi di layar,
        // tetapi tidak mem-post notification OS kedua kalinya.
        if (FirebaseApp.getApps(context).isNotEmpty()) return
        if (notifications.isEmpty()) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefKey = "shown_ids_${roleOrUserKey.lowercase()}"
        val shownSet = prefs.getStringSet(prefKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        var changed = false

        // Show up to 3 newest unread notifications that haven't been shown on this device yet
        notifications
            .filter { !it.isRead && it.id > 0 }
            .take(3)
            .forEach { n ->
                val idStr = n.id.toString()
                if (!shownSet.contains(idStr)) {
                    shownSet.add(idStr)
                    changed = true
                    showSystemNotification(
                        context = context,
                        notificationId = (n.id % Int.MAX_VALUE).toInt(),
                        title = n.title.ifBlank { "PT Integra Cipta Data" },
                        message = n.message
                    )
                }
            }

        if (changed) {
            prefs.edit().putStringSet(prefKey, shownSet).apply()
        }
    }
}
