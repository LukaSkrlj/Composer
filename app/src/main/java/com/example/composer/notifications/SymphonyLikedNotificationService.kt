package com.example.composer.notifications

import android.Manifest
import com.example.composer.R
import android.app.Notification.Builder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class SymphonyLikedNotificationService : FirebaseMessagingService() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        var notificationTitle: String? = null
        var notificationBody: String? = null

        // Check if message contains a notification payload
        if (remoteMessage.notification != null) {
            notificationTitle = remoteMessage.notification?.title
            notificationBody = remoteMessage.notification?.body
        }
        val CHANNEL_ID = "HEADS_UP_NOTIFICATION"
        val channel =
            NotificationChannel(CHANNEL_ID, "postLiked", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification = Builder(this, CHANNEL_ID).setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(1, notification.build())
    }

}

