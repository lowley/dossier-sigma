package lorry.folder.items.dossiersigma.headless.service.utilities

import androidx.datastore.preferences.protobuf.Timestamp

data class SigmaNotification(
    val notificationId: Int,
    val title: String,
    val text: String,
    val smallIconRes: Int,
    val timestamp: Timestamp,
    val progress: Pair<Int, Int>? = null,
    val isOngoing: Boolean = false,
)