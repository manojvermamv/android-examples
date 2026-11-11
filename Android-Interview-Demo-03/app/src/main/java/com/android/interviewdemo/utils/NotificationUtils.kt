package com.android.interviewdemo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat

class NotificationUtils(private val context: Context) {

    private val notificationManager get() = NotificationManagerCompat.from(context)

    // Create a notification channel (required for Android 8+)
    fun createChannel(
        channelId: String,
        channelName: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        showBadge: Boolean = true,
        lights: Boolean = true,
        vibration: Boolean = true
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                setShowBadge(showBadge)
                enableLights(lights)
                lightColor = Color.RED
                enableVibration(vibration)
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    // DSL-style notification builder
    fun notification(channelId: String, config: NotificationBuilder.() -> Unit): NotificationCompat.Builder {
        return NotificationBuilder(channelId).apply(config).build(context)
    }

    // Show the notification
    fun show(notificationId: Int, builder: NotificationCompat.Builder) {
        notificationManager.notify(notificationId, builder.build())
    }

    // Notification Builder DSL
    inner class NotificationBuilder(private val channelId: String) {
        private var title: String = ""
        private var content: String = ""
        private var smallIcon: Int = -1
        private var largeIcon: Bitmap? = null
        private var autoCancel: Boolean = true
        private var priority: Int = NotificationCompat.PRIORITY_DEFAULT
        private val actions = mutableListOf<NotificationCompat.Action>()
        private var style: NotificationCompat.Style? = null
        private var progressMax: Int = 0
        private var progressCurrent: Int = 0
        private var progressIndeterminate: Boolean = false
        private var timeoutAfter: Long? = null
        private var badgeIconType: Int = NotificationCompat.BADGE_ICON_NONE
        private var groupKey: String? = null
        private var isGroupSummary: Boolean = false

        // Basic notification properties
        fun title(title: String) {
            this.title = title
        }

        fun content(content: String) {
            this.content = content
        }

        fun icon(@DrawableRes iconRes: Int) {
            this.smallIcon = iconRes
        }

        fun largeIcon(bitmap: Bitmap) {
            this.largeIcon = bitmap
        }

        fun priority(level: Int) {
            this.priority = level
        }

        fun autoCancel(autoCancel: Boolean) {
            this.autoCancel = autoCancel
        }

        // Add actions to the notification
        fun action(icon: IconCompat, title: String, pendingIntent: PendingIntent) {
            actions.add(NotificationCompat.Action.Builder(icon, title, pendingIntent).build())
        }

        // Group conversation style
        fun groupConversation(messages: List<Pair<String, String>>, user: Person) {
            val notificationStyle = NotificationCompat.MessagingStyle(user)
            messages.forEach { (sender, message) ->
                notificationStyle.addMessage(message, System.currentTimeMillis(), Person.Builder().setName(sender).build())
            }
            style = notificationStyle
        }

        // Progress bar
        fun progress(max: Int, current: Int, indeterminate: Boolean = false) {
            this.progressMax = max
            this.progressCurrent = current
            this.progressIndeterminate = indeterminate
        }

        // Notification timeout
        fun timeoutAfter(duration: Long) {
            this.timeoutAfter = duration
        }

        // Badge icon type
        fun badgeIconType(type: BadgeIconType) {
            this.badgeIconType = type.value
        }

        // Grouping notifications
        fun group(groupKey: String, isGroupSummary: Boolean = false) {
            this.groupKey = groupKey
            this.isGroupSummary = isGroupSummary
        }

        // Build the notification
        fun build(context: Context): NotificationCompat.Builder {
            return NotificationCompat.Builder(context, channelId).apply {
                setContentTitle(title)
                setContentText(content)
                setSmallIcon(smallIcon)
                setLargeIcon(largeIcon)
                setAutoCancel(autoCancel)
                priority = this@NotificationBuilder.priority
                style?.let { setStyle(it) }
                actions.forEach { addAction(it) }

                // Progress
                if (progressMax > 0) {
                    setProgress(progressMax, progressCurrent, progressIndeterminate)
                }

                // Timeout After
                timeoutAfter?.let { setTimeoutAfter(it) }

                // Badge
                setBadgeIconType(badgeIconType)

                // Grouping
                groupKey?.let { setGroup(it) }
                if (isGroupSummary) {
                    setGroupSummary(true)
                }
            }
        }
    }

    // Badge icon type enum
    enum class BadgeIconType(val value: Int) {
        NONE(NotificationCompat.BADGE_ICON_NONE),
        SMALL(NotificationCompat.BADGE_ICON_SMALL),
        LARGE(NotificationCompat.BADGE_ICON_LARGE)
    }
}



/**
 * Extension functions for easier usage
 * */
// Create a notification channel
fun Context.createNotificationChannel(
    channelId: String,
    channelName: String,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) = NotificationUtils(this).createChannel(channelId, channelName, importance)

// Show a notification
fun Context.showNotification(
    channelId: String,
    notificationId: Int = System.currentTimeMillis().toInt(),
    config: NotificationUtils.NotificationBuilder.() -> Unit
) {
    NotificationUtils(this).apply {
        show(notificationId, notification(channelId, config))
    }
}

fun NotificationUtils.NotificationBuilder.timeout(
    timeoutAfter: Long,
    autoCancel: Boolean = true
) {
    timeoutAfter(timeoutAfter)
    autoCancel(autoCancel)
}


/**
 * **1. Heads-up Notifications**
 * Show high-priority notifications.
 *
 * ```kotlin
 * priority(NotificationCompat.PRIORITY_HIGH)
 * setFullScreenIntent(pendingIntent, true)
 * ```
 *
 * **2. Big Text Style**
 * Expandable notifications with large text.
 *
 * ```kotlin
 * style = NotificationCompat.BigTextStyle().bigText(longTextContent)
 * ```
 *
 * **3. Inbox Style**
 * Display multiple lines of text.
 *
 * ```kotlin
 * style = NotificationCompat.InboxStyle()
 *     .addLine("First message")
 *     .addLine("Second message")
 * ```
 *
 * **4. Custom Styles**
 * Support for custom notification styles.
 *
 * ```kotlin
 * style = NotificationCompat.DecoratedCustomViewStyle()
 * ```
 * */
fun showSampleNotification(context: Context, replyIntent: PendingIntent) {
    /**
     * **1. Notification Channels**
     * */
    context.createNotificationChannel(
        channelId = "messages",
        channelName = "Chat Messages",
        importance = NotificationManager.IMPORTANCE_HIGH
    )

    /**
     * **2. DSL-Style Notification Building**
     * */
    context.showNotification("messages_channel") {
        title("New Message")
        content("You have a new message from John")
        icon(android.R.drawable.ic_dialog_email)
        priority(NotificationCompat.PRIORITY_HIGH)

        // Notification Grouping, Group related notifications.
        group(groupKey = "message_group", isGroupSummary = true)

        // Custom Actions
        // Add clickable actions to notifications.
        action(
            icon = IconCompat.createWithResource(context, android.R.drawable.ic_menu_send),
            title = "Reply",
            pendingIntent = replyIntent
        )

        // Group Conversations
        // Display chat-style notifications.
        groupConversation(
            messages = listOf(
                "Alice" to "Meeting at 3?",
                "You" to "Yes, see you there!"
            ),
            user = Person.Builder()
                .setName("You")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon))
                .build()
        )

        // Progress Notifications
        // Show progress bars in notifications.
        progress(max = 100, current = 50, indeterminate = false)

        // Notification Timeouts
        // Automatically dismiss notifications after a timeout.
        timeoutAfter(duration = 5000L) // 5 seconds

        // Badge Icons
        // Customize badge icons for notifications.
        badgeIconType(NotificationUtils.BadgeIconType.SMALL)
    }
}