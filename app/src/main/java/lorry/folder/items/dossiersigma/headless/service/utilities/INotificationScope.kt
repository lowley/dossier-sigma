package lorry.folder.items.dossiersigma.headless.service.utilities

interface INotificationScope {
    fun showNotificationById(notificationId: Int)
}

typealias CoreContent = suspend INotificationScope.() -> Unit
