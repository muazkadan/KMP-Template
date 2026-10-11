package dev.muazkadan.myapplication.data.notifications

/**
 * Shows the user a notification through the system. Bound only on desktop: Android and iOS post
 * theirs through their own notification APIs, after asking for permission.
 */
interface Notifier {
    suspend fun show(notification: Notification)
}

data class Notification(
    val title: String,
    val message: String,
    val withSound: Boolean = true,
    /** A button, on Linux and Windows. macOS notifications have none; a click opens the app. */
    val action: NotificationAction? = null,
)

/** A notification's button. Its [id] reaches the app when the user clicks it. */
data class NotificationAction(
    val id: String,
    val label: String,
)
