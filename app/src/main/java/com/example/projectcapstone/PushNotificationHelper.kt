package com.example.projectcapstone

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.UUID

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

    /**
     * Obtains a real FCM token if FirebaseMessaging is available on classpath,
     * or generates a persistent device token so `POST /api/notifications/device`
     * always populates `users.fcm_token` in MySQL (matching Sequence Diagram 3.6).
     */
    fun obtainDeviceFcmToken(context: Context, onToken: (String) -> Unit) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Try FirebaseMessaging via reflection first if configured
        val reflected = runCatching {
            val clazz = Class.forName("com.google.firebase.messaging.FirebaseMessaging")
            val instance = clazz.getMethod("getInstance").invoke(null)
            val task = clazz.getMethod("getToken").invoke(instance)
            val taskClass = Class.forName("com.google.android.gms.tasks.Task")
            val isComplete = taskClass.getMethod("isComplete").invoke(task) as? Boolean ?: false
            if (isComplete) {
                taskClass.getMethod("getResult").invoke(task) as? String
            } else null
        }.getOrNull()

        if (!reflected.isNullOrBlank()) {
            prefs.edit().putString("fcm_token", reflected).apply()
            onToken(reflected)
            return
        }

        val existing = prefs.getString("fcm_token", null)
        if (!existing.isNullOrBlank()) {
            onToken(existing)
            return
        }

        val androidId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString().replace("-", "")

        val generated = "fcm_android_${Build.MODEL.replace(" ", "_")}_$androidId"
        prefs.edit().putString("fcm_token", generated).apply()
        onToken(generated)
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
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
