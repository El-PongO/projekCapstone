package com.example.projectcapstone

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Menangani FCM saat app foreground dan perubahan token.
 * Saat background, notification payload ditampilkan otomatis oleh Android/FCM.
 */
class IntegraFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isNotBlank()) {
            Log.d("IntegraFCM", "Token FCM baru diterima (token tidak dicetak)")
            ApiService.registerRefreshedFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Integra Agency"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        if (body.isBlank()) return
        val notificationId = message.messageId?.hashCode()?.and(Int.MAX_VALUE)
            ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        PushNotificationHelper.showSystemNotification(this, notificationId, title, body)
    }
}
