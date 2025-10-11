package lorry.folder.items.dossiersigma.headless.shortcuts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import lorry.folder.items.copieurtho2.R
import lorry.folder.items.copieurtho2.__data.userPreferences.DSI_UserPreferences
import lorry.folder.items.copieurtho2.components.collectToState
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.headless.domain.EmptyItem
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFile
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutUseCase @Inject constructor(
    val ftpDataSource: DSI_FTP,
    private val fileRepo: IDiskRepository,
    private val userPreferences: DSI_UserPreferences,
    private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    val destinationShortcuts = mutableMapOf<String, MutableSet<String>>()
    val currentFileShortcuts = mutableSetOf<String>()

    private val _destinationFolders = MutableStateFlow<Set<String>>(emptySet())
    val destinationFolders: StateFlow<Set<String>> = _destinationFolders

    private val _storageFolder = MutableStateFlow("")
    val storageFolder: StateFlow<String> = _storageFolder

    companion object {
        const val TAG = "ShrtcutUC"
    }


    suspend fun createDestinationShortcutInventory() {

        destinationShortcuts.clear()
        var secondLevel = destinationFolders.value.flatMap { firstLevel ->
            fileRepo.getFolderItems(firstLevel, SortingCriterion.ByDateDesc)
        }

        for (dir in secondLevel) {
            val parts = dir.fullPath
                .substringAfterLast('/')   // ne garde que le nom
                .substringAfterLast('\\')  // ne garde que le nom
                .split('.')
            parts.forEach {
                destinationShortcuts.getOrPut(it) { mutableSetOf() }.add(dir)
            }
        }
    }

    suspend fun traiteVideo(
        video: SigmaFile,
        coverBitmapUrl: String? = null,
        coverBase64: String? = null,
        rootDir: String = "/storage/7376-B000/SEXE 2",
    ) {
        var root = rootDir
        if (!File(root).exists())
            root = "/storage/7376-B000/SEXE 2"
        if (!File(root).exists())
            root = "/storage/6539-3963"
        if (!File(root).exists())
            throw Exception("Le dossier racine n'existe pas")

        parseVideoName(video)

        var coverBitmap: Bitmap? = null
        if (coverBitmapUrl != null)
            coverBitmap = urlToBitmapSuspend(coverBitmapUrl)

        //les shortcuts simples
        currentFileShortcuts.filter { sc -> !sc.startsWith('+') }.forEach { sc ->
            if (destinationShortcuts.containsKey(sc)) {
                destinationShortcuts.getValue(sc).forEach { dest ->
                    val correctVideoName = Uri.encode(video.name)

                    val videoNamePartsWithStars =
                        currentFileShortcuts.filter { sc -> sc.startsWith('+') }
                    var modifiedVideoName = video.name
                    videoNamePartsWithStars.forEach { part ->
                        val newPart = part
                            .split('+')
                            .filter { subPart -> subPart.isNotEmpty() }
                            .drop(1).take(1)[0]
                        Log.d(TAG, "newPart: $newPart")

                        modifiedVideoName = modifiedVideoName
                            .replace(part, newPart)
                    }

                    val destFullPath = "$dest/${modifiedVideoName.substringBeforeLast(".")}.html"
                    val encodedMp4 = "/videos/$correctVideoName"

                    Log.d(TAG, "création HTML: $destFullPath")

                    fileRepo.createShortcut(
                        text(encodedMp4, "vlc", coverBitmap, coverBase64),
                        destFullPath
                    )
                }
            }
        }

        //shortcuts de création
        currentFileShortcuts.filter { sc -> sc.startsWith('+') }.forEach { sc ->
            val parts = sc.split('+').filter { it.isNotEmpty() }
            val firstLevel = parts[0]
            val totalFirstLevel = "$root/$firstLevel"

            var goodFirstLevel = destinationFolders.value
                .firstOrNull { folder -> folder.endsWith(firstLevel) }

            var hasGoodFirstLevel = goodFirstLevel != null

            //premier level inconnu
            if (!hasGoodFirstLevel) {
                addDestination(totalFirstLevel)
            }

            val otherParts = parts.drop(1)

            val secondLevels: List<String> = fileRepo.getFolderItems(totalFirstLevel,
                SortingCriterion.ByNameAsc).filter { it.isFolder() }.map { it.fullPath }

            val found = secondLevels.firstOrNull { secondLevel ->
                return@firstOrNull otherParts.all { part ->
                    secondLevel.contains(part)
                }
            }

            //secondLevel pas trouvé : il faut le créer
            if (found == null)
                File("$totalFirstLevel/${otherParts.joinToString(".")}").mkdir()

            //shorten
            val videoNamePartsWithStars =
                currentFileShortcuts.filter { sc -> sc.startsWith('+') }
            var modifiedVideoName = video.name
            videoNamePartsWithStars.forEach { part ->
                val newPart = part
                    .split('+')
                    .filter { subPart -> subPart.isNotEmpty() }
                    .drop(1).take(1).get(0)
                Log.d(TAG, "newPart: $newPart")

                modifiedVideoName = modifiedVideoName
                    .replace(part, newPart)
            }

            //create shortcuts
            val correctVideoName = Uri.encode(modifiedVideoName)
            val destPath = "$totalFirstLevel/${otherParts.joinToString(".")}"
            val destFullPath = "$destPath/${modifiedVideoName.replace(".mp4", ".html")}"
            val encodedMp4 = "/videos/$correctVideoName"
//            fileRepo.createShortcut(
//                text(encodedMp4, "bsplayer", coverBitmap, coverBase64),
//                destFullPath.replace(".mp4", " .html")
//            )
            fileRepo.createShortcut(
                text(encodedMp4, "vlc", coverBitmap, coverBase64),
                destFullPath
            )
        }
    }

    suspend fun writeOneHtmlWithPictureForVideo(
        video: ThoFile, coverBitmapUrl: String? = null,
        coverBase64: String? = null
    ) {
        var coverBitmap: Bitmap? = null
        if (coverBitmapUrl != null)
            coverBitmap = urlToBitmapSuspend(coverBitmapUrl)

        val correctVideoName = Uri.encode(video.name)

        val destFullPath = "/videos/pictures/${video.name.replace(".mp4", ".html")}"
        val encodedMp4 = "/videos/$correctVideoName"
        ftpDataSource.createShortcut(
            text(encodedMp4, "vlc", coverBitmap, coverBase64),
            destFullPath
        )
    }

    private suspend fun addDestination(totalFirstLevel: String) {

        fileRepo.createPath(totalFirstLevel)
        userPreferences.add_destination_folder(totalFirstLevel)
    }

    fun parseVideoName(video: SigmaFile) {
        currentFileShortcuts.clear()
        currentFileShortcuts.addAll(
            video.name
                .substringAfter('.')       //après nom principal
                .substringBeforeLast(".")   //avant .mp4
                .split('.')
                .toMutableSet()
        )
        currentFileShortcuts.add("tous")
    }

    /**
     * pour image: prends le coverBase64 si non nul, sinon se base sur coverBitmap
     */
    fun text(
        fullPath: String,
        player: String,
        coverBitmap: Bitmap? = null,
        coverBase64: String? = null
    ): String {
        var nas = "smb://admin:37-2lematin@192.168.1.20${fullPath}?player=$player"

        //assert
        val base64Cover: String? = coverBitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        }

        val actualBase64 = coverBase64 ?: base64Cover

        val imageSection = actualBase64?.let {
            """<img src="data:image/jpeg;base64,$it" alt="cover" style="max-width:100%;height:auto;"/><br>"""
        } ?: ""

        val text = """<!DOCTYPE html>
                                 <html lang="fr">
                                 <head>
                                     <meta charset="UTF-8">
                                     <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                     <title>Redirection automatique</title>
                                 </head>
                                 <body>
                                $imageSection
                                 <a id="autoClickLink" href="myapp://playvideo/kiwi?video=$nas">Lien automatique</a>

                                 <script>
                                     window.onload = function() {
                                         // Récupère le lien par son identifiant et déclenche le clic
                                         document.getElementById("autoClickLink").click();
                                     };
                                 </script>

                                 </body>
                                 </html>"""

        return text
    }

    suspend fun urlToBitmapSuspend(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val input = URL(imageUrl).openStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            println("Erreur lors du chargement de l'image : ${e.message}")
            null
        }
    }

    fun reset() {
        destinationShortcuts.clear()
    }

    val destinationShortcutsCount: Int
        get() = destinationShortcuts.keys.count()

    val currentVideoShortcutCount: Int
        get() = currentFileShortcuts.count()

    val currentVideoParts
        get() = currentFileShortcuts.toSet()

    /**
     * Récupère les fullPathName des répertoires correspondant au shortcut
     */
    fun getDirectoriesFor(video: ThoFile): Set<String> {
        val videoName = video.name
        val shortcuts = videoName
            .substringAfter('.')       //après nom principal
            .substringBeforeLast('.')  //avant .mp4
            .split('.')

        val destinationDirs = mutableSetOf<String>()

        for (shortcut in shortcuts) {
            destinationShortcuts[shortcut]?.let { destinationDirs.addAll(it) }
        }
        return destinationDirs
    }

    suspend fun deleteHtmlFilesInDestinations() {
        println("début des suppressions")

        fun generateNotification(): (Int, String) -> Unit {
            var notificationCount = 0
            var lastNotificationMillis: Long = 0
            return { deletedFilesCount: Int, name: String ->
                if (System.currentTimeMillis() - lastNotificationMillis >= 1000) {
                    updateNotification(
                        "($deletedFilesCount) Suppression de $name",
                        if (notificationCount % 2 == 0) R.drawable.medicament_haut else R.drawable.medicament_bas
                    )
                    lastNotificationMillis = System.currentTimeMillis()
                    notificationCount++
                }
            }
        }

        var deletedFilesCount = 0
        val notificationGenerator = generateNotification()

        destinationFolders.value.forEach { firstLevel ->
            println("Chemin testé : $firstLevel")
            val fullSecondLevels = buildSecondLevels(firstLevel)

            fullSecondLevels?.forEach { secondLevel ->
                println("secondLevel: $secondLevel")
                val htmls = fileRepo.fetchFiles(secondLevel, "html")

                htmls?.forEach { file ->
                    notificationGenerator(deletedFilesCount, file.name)
                    fileRepo.deleteIfNotFolderPictureHtml(file)
                    deletedFilesCount++
                    println("Suppression de ${file.fullPath} ($deletedFilesCount)")
                }
            }
        }
        println("$deletedFilesCount fichiers supprimés")
        updateNotification("En attente...", R.drawable.masque)
        println("fin des suppressions")
    }

    private suspend fun buildSecondLevels(firstLevel: String): List<String>? {
        var fullSecondLevels = fileRepo.fetchDirectories(firstLevel)
        println("secondLevels: ${fullSecondLevels.size}")
        return fullSecondLevels
    }

    suspend fun createHtmlFilesInDestinations() {
        println("début des générations")

        fun generateNotification(): (Int, String) -> Unit {
            var notificationCount = 0
            var lastNotificationMillis: Long = 0
            return { createdShortcutsCount: Int, name: String ->
                if (System.currentTimeMillis() - lastNotificationMillis >= 1000) {
                    updateNotification(
                        "($createdShortcutsCount) Création de $name",
                        if (createdShortcutsCount % 2 == 0) R.drawable.medicament_haut else R.drawable.medicament_bas
                    )
                    lastNotificationMillis = System.currentTimeMillis()
                    notificationCount++
                }
            }
        }

        var createdShortcutsCount = 0
        val notificationGenerator = generateNotification()

        createDestinationShortcutInventory()
        var videos = ftpDataSource.fetchMP4Files(storageFolder.value)

        videos?.forEach {
            notificationGenerator(createdShortcutsCount, it.name)
            traiteVideo(it)
            createdShortcutsCount++
            println("($createdShortcutsCount) - Création de ${it.name}")
        }
        updateNotification("En attente...", R.drawable.masque)
        println("$createdShortcutsCount vidéos traitées pour raccourci html")
        println("fin des générations")
    }

    fun updateNotification(message: String, icon: Int) {
        showOrUpdateNotification(
            context,
            message,
            icon
        )  // Utilisation directe
    }

    fun showOrUpdateNotification(context: Context, message: String, icon: Int) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channelId =
            "my_foreground_service_channel" // Doit être le même pour toutes les mises à jour
        val notificationId = 1 // Identifiant unique de la notification

        // ⚡ Vérifier si le canal existe (obligatoire sur Android 8+)
        val channel = NotificationChannel(
            channelId,
            "Service en cours",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        // 🎨 Construire la notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle("Copieur Tho v2")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // 🔄 Mettre à jour ou afficher la notification
        notificationManager.notify(notificationId, notification)
    }

    suspend fun manageHtmlFilesInDestinations(
        copyPictures: Boolean = false,
        rootDir: String,
        onlyOneFileShortcutFile: Item = EmptyItem
    ) {
        Log.d(TAG, "début des générations améliorées")

        val onlyOneFile = onlyOneFileShortcutFile != EmptyItem
        if (onlyOneFile)
            assert(onlyOneFileShortcutFile.picture is String)

        var createdShortcutsCount = 0

        createDestinationShortcutInventory()
        Log.d(TAG, "dest shortcuts: ${destinationShortcuts.keys.size}")

        var videos = if (onlyOneFile)
            listOf(onlyOneFileShortcutFile)
        else
            ftpDataSource.fetchFiles(storageFolder.value) ?: emptyList()

        Log.d(TAG, "videos: ${videos.size}")

        val imageGroups: MutableMap<String, String> = mutableMapOf()

        val mapFileFullPathToShortcuts: MutableMap<String, List<String>> = mutableMapOf()
        videos.forEachDebug { video ->
            mapFileFullPathToShortcuts.put(
                video.fullPath,
                video.fullPath.substringBefore(".mp4").substringAfter(".").split(".") + "tous"
            )
        }

        videos.forEachDebug { nasVideo ->
            //map: videos -> liste des répertoires de ses shortcuts

            //mappe un dossier de vidéo aux répertoires de shortcuts qui correspondent aux shortcuts de cette vidéo
            //dit où créer des shortcuts pour cette vidéo
            val mapFullPathToShortcutForNasVideo =
                destinationShortcuts.filterKeys { shortcut ->
                    mapFileFullPathToShortcuts[nasVideo.fullPath]?.contains(
                        shortcut
                    ) == true
                }

            //répertoire où créer un shortcut pour ce fichier nasVideo
            mapFullPathToShortcutForNasVideo.values.flatten().forEachDebug { folderFullPath ->
                //collecte tous les html du répertoire
                fileRepo.getFolderItems(folderFullPath, SortingCriterion.ByNameAsc).forEachDebug { collectedHtmlFile ->
                    //html existe dans collectés? oui -> recup image base64 si possible
                    //ajout dans mutableMap <fichier html, image base64>
                    val imageBase64: String? = getBase64InHtml(collectedHtmlFile)

                    if (onlyOneFile && onlyOneFileShortcutFile.picture != null)
                        imageGroups.put(nasVideo.fullPath, onlyOneFileShortcutFile.picture as String)
                    else
                        if (collectedHtmlFile.name.replace(".html", "").trim()
                            == nasVideo.name.substringBeforeLast(".").trim() &&
                            imageBase64 != null
                        )
                            imageGroups.put(nasVideo.fullPath, imageBase64)
                }

                //notificationGenerator(createdShortcutsCount, htmlFile.name)
            }
        }

        Log.d(TAG, "après ajouts, imageGroups: ${imageGroups.size}")

        videos.forEachDebug() { nasVideo ->

            //restriction de destinationShortcuts aux entrées dont la clé (shortcut) est l'un des shortcuts de la vidéo nasVideo
            //nasVideo -> shortcut -> destinationFolder(s)
            val mapShortcutToDirectories =
                destinationShortcuts.filterKeys { shortcut ->
                    mapFileFullPathToShortcuts[nasVideo.fullPath]?.contains(
                        shortcut
                    ) == true
                }

            //création de chaque html avec image si existe dans mutableMap
            //répertoire où devraient être les shortcuts
            mapFileFullPathToShortcuts[nasVideo.fullPath]?.map { sc -> mapShortcutToDirectories[sc] }
                ?.forEachDebug { folder ->
                    val htmlFile = SigmaFile(
                        fullPath = "${nasVideo.fullPath.substringBeforeLast(".")}.html",
                        picture = null,
                        modificationDate = 0L,
                        tag = null,
                        scale = null,
                        memo = null
                    )


//                    val htmlFile = ThoFile(
//                        name = nasVideo.name,
//                        timestamp = Calendar.Builder().build(),
//                        size = 0,
//                        fullPath = "${nasVideo.fullPath.substringBeforeLast(".")}.html",
//                        isVideoFile = false,
//                        isHtmlFile = true,
//                        null
//                    )

                    val retrievedImage = imageGroups[nasVideo.fullPath]
                    if (retrievedImage != null)
                        Log.d(
                            TAG,
                            "création de ${htmlFile.fullPath} avec image ${retrievedImage.length}"
                        )
                    else
                        Log.d(TAG, "création de ${htmlFile.fullPath} sans image")

                    traiteVideo(
                        htmlFile,
                        coverBase64 = retrievedImage,
                        rootDir = rootDir
                    )

                    Log.d(TAG, "${htmlFile.name} traité")
                }
        }

//        updateNotification("En attente...", R.drawable.masque)
//        println("$createdShortcutsCount vidéos traitées pour raccourci html")
//        println("fin des générations")

    }

    suspend fun importHtmlsWithImagesFromNAS(display: suspend (String) -> Unit, rootDir: String) {

        println("début des générations améliorées")

        fun generateNotification(): (Int, String) -> Unit {
            var notificationCount = 0
            var lastNotificationMillis: Long = 0
            return { createdShortcutsCount: Int, name: String ->
                if (System.currentTimeMillis() - lastNotificationMillis >= 1000) {
                    updateNotification(
                        "($createdShortcutsCount) Création de $name",
                        if (createdShortcutsCount % 2 == 0) R.drawable.medicament_haut else R.drawable.medicament_bas
                    )
                    lastNotificationMillis = System.currentTimeMillis()
                    notificationCount++
                }
            }
        }

        //lecture du NAS, récup des htmls

        var createdShortcutsCount = 0
        val notificationGenerator = generateNotification()

        createDestinationShortcutInventory()
        println("THO: dest shortcuts: ${destinationShortcuts.keys.size}")

        display("Récupération des images sur le NAS...")

        var videos = ftpDataSource.fetchHtmlFiles("/videos/pictures", display) ?: emptyList()
        println("THO: videos: ${videos.size}")

        val imageGroups: MutableMap<String, String> = mutableMapOf()

        val mapFileFullpathToShortcuts: MutableMap<String, List<String>> = mutableMapOf()
        videos.forEach { video ->
            mapFileFullpathToShortcuts.put(
                video.fullPath,
                video.fullPath.substringBefore(".html").substringAfter(".").split(".") + "tous"
            )
        }

        videos.forEach { nasVideo ->
            if (!nasVideo.pictureBase64.isNullOrEmpty()) {
                imageGroups.put(nasVideo.fullPath, nasVideo.pictureBase64)
            }
        }

        videos.forEach { nasVideo ->
            //map: videos -> liste des répertoires de ses shortcuts

            val mapShortcutsToDirectories =
                destinationShortcuts.filterKeys { shortcut ->
                    mapFileFullpathToShortcuts[nasVideo.fullPath]?.contains(
                        shortcut
                    ) == true
                }

            println("THO: après ajouts, imageGroups: ${imageGroups.size}")

            display("suppression des htmls...")

            //suppression de tous les html
            //répertoire où devraient être les shortcuts
            destinationFolders.value.forEach()
            { folder ->
                fileRepo.fetchFiles(folder, "html")?.forEach { htmlFile ->
                    if (mapShortcutsToDirectories.keys.contains(htmlFile.fullPath))
                        fileRepo.deleteIfNotFolderPictureHtml(htmlFile)
                }

                File(folder).listFiles().filter { it.isDirectory }.forEach { folder2nd ->
                    fileRepo.fetchFiles(folder2nd.absolutePath, "html")?.forEach { htmlFile ->
                        if (mapShortcutsToDirectories.keys.contains(htmlFile.fullPath))
                            fileRepo.deleteIfNotFolderPictureHtml(htmlFile)
                    }
                }
            }

            display("htmls supprimés")

            videos.forEach() { nasVideo ->
                display("Traitement de ${nasVideo.name}")

                val mapShortcutToDirectories =
                    destinationShortcuts.filterKeys { shortcut ->
                        mapFileFullpathToShortcuts[nasVideo.fullPath]?.contains(
                            shortcut
                        ) == true
                    }

                //création de chaque html avec image si existe dans mutableMap
                //répertoire où devraient être les shortcuts
                mapFileFullpathToShortcuts[nasVideo.fullPath]?.map { sc -> mapShortcutToDirectories[sc] }
                    ?.forEach { folder ->
                        val htmlFile = ThoFile(
                            name = nasVideo.name,
                            timestamp = Calendar.Builder().build(),
                            size = 0,
                            fullPath = nasVideo.fullPath,
                            isVideoFile = false,
                            isHtmlFile = true,
                            null
                        )
                        val retrievedImage = imageGroups[nasVideo.fullPath]
                        if (retrievedImage != null || nasVideo.pictureBase64 != null) {
                            display("THO: création de ${htmlFile.name} avec image ${retrievedImage?.length}")

                            traiteVideo(
                                htmlFile,
                                coverBase64 = retrievedImage,
                                rootDir = rootDir
                            )
                        }

                        println("THO: ${htmlFile.name} traité")
                    }

            }

//        updateNotification("En attente...", R.drawable.masque)
//        println("$createdShortcutsCount vidéos traitées pour raccourci html")
//        println("fin des générations")

        }
    }

    suspend fun copyPicturesToNAS() {
        println("début de la copie")

        fun generateNotification(): (Int, String) -> Unit {
            var notificationCount = 0
            var lastNotificationMillis: Long = 0
            return { createdShortcutsCount: Int, name: String ->
                if (System.currentTimeMillis() - lastNotificationMillis >= 1000) {
                    updateNotification(
                        "($createdShortcutsCount) Création de $name",
                        if (createdShortcutsCount % 2 == 0) R.drawable.medicament_haut else R.drawable.medicament_bas
                    )
                    lastNotificationMillis = System.currentTimeMillis()
                    notificationCount++
                }
            }
        }

        var createdShortcutsCount = 0
        val notificationGenerator = generateNotification()

        createDestinationShortcutInventory()
        println("THO: dest shortcuts: ${destinationShortcuts.keys.size}")

        var videos = ftpDataSource.fetchMP4Files(storageFolder.value) ?: emptyList()
        println("THO: videos: ${videos.size}")

        val imageGroups: MutableMap<String, String> = mutableMapOf()

        val mapFileFullpathToShortcuts: MutableMap<String, List<String>> = mutableMapOf()
        videos.forEach { video ->
            mapFileFullpathToShortcuts.put(
                video.fullPath,
                video.fullPath.substringBefore(".mp4").substringAfter(".").split(".") + "tous"
            )
        }

        videos.forEach { nasVideo ->
            //map: videos -> liste des répertoires de ses shortcuts

            val mapShortcutsToDirectories =
                destinationShortcuts.filterKeys { shortcut ->
                    mapFileFullpathToShortcuts[nasVideo.fullPath]?.contains(
                        shortcut
                    ) == true
                }

            //répertoire où peuvent être les shortcuts de ce fichier nasVideo
            mapShortcutsToDirectories.values.flatten().forEach { folder ->
                //collecte tous les html du répertoire
                fileRepo.fetchFiles(folder, "html")?.forEach { collectedHtmlFile ->


                    //html existe dans collectés? oui -> recup image base64 si possible
                    //ajout dans mutableMap <fichier html, image base64>
                    val imageBase64: String? = fileRepo.getBase64InHtml(collectedHtmlFile)
                    if (collectedHtmlFile.name.replace(".html", "").trim()
                        == nasVideo.name.replace(".mp4", "").trim() &&
                        imageBase64 != null
                    )
                        imageGroups.put(nasVideo.fullPath, imageBase64)
                }

                //notificationGenerator(createdShortcutsCount, htmlFile.name)
            }
        }

        println("THO: après ajouts, imageGroups: ${imageGroups.size}")

        videos.forEach() { nasVideo ->
            val mapShortcutToDirectories =
                destinationShortcuts.filterKeys { shortcut ->
                    mapFileFullpathToShortcuts[nasVideo.fullPath]?.contains(
                        shortcut
                    ) == true
                }

            //création de chaque html avec image si existe dans mutableMap
            //répertoire où devraient être les shortcuts
            mapFileFullpathToShortcuts[nasVideo.fullPath]?.map { sc -> mapShortcutToDirectories[sc] }
                ?.forEach { folder ->
                    val htmlFile = ThoFile(
                        name = nasVideo.name,
                        timestamp = Calendar.Builder().build(),
                        size = 0,
                        fullPath = nasVideo.fullPath.replace(".mp4", ".html"),
                        isVideoFile = false,
                        isHtmlFile = true,
                        null
                    )
                    val retrievedImage = imageGroups[nasVideo.fullPath]
                    if (retrievedImage != null)
                        println("THO: création de ${htmlFile.name} avec image ${retrievedImage.length}")
                    else
                        println("THO: création de ${htmlFile.name}")

                    if (retrievedImage != null)
                        writeOneHtmlWithPictureForVideo(
                            htmlFile,
                            coverBase64 = retrievedImage
                        )

                    println("THO: ${htmlFile.name} traité")
                }
        }
    }

    init {
        scope.collectToState(userPreferences.destination_folders, _destinationFolders)
        scope.collectToState(userPreferences.storage_folder, _storageFolder)
    }
}

suspend fun <T> Iterable<T>.forEachDebug(action: suspend (T) -> Unit) {
    for (element in this) action(element)
}

suspend fun getBase64InHtml(file: Item): String? {
    val htmlFile = File(file.fullPath)
    val htmlContent = htmlFile.readText()

    // Regex pour trouver le contenu de src="data:image/...;base64,..."
    val regex = Regex("""<img\s+[^>]*src\s*=\s*"data:image/[^;]+;base64,([^"]+)"""")
    val match = regex.find(htmlContent) ?: return null
    val base64Image = match.groupValues[1]

    return base64Image
}