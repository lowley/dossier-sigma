package lorry.folder.items.dossiersigma.headless.shortcuts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.external.userPreferences.DSI_UserPreferences
import lorry.folder.items.dossiersigma.headless.domain.EmptyItem
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFile
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.domain.lastSegment
import lorry.folder.items.dossiersigma.headless.domain.str
import lorry.folder.items.dossiersigma.headless.domain.toSigmaPath
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutUseCase @Inject constructor(
    val ftpDataSource: DSI_FTP,
    private val fileRepo: IDiskRepository,
    private val userPreferences: DSI_UserPreferences,
) {
    val destinationShortcuts = mutableMapOf<String, MutableSet<SigmaPath>>()
    val currentFileShortcuts = mutableSetOf<String>()

    private val _destinationFolders = MutableStateFlow<Set<SigmaPath>>(emptySet())
    val destinationFolders: StateFlow<Set<SigmaPath>> = _destinationFolders

    private val _storageFolder = MutableStateFlow("".toSigmaPath())
    val storageFolder: StateFlow<SigmaPath> = _storageFolder

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    ///////////////////////////////////////////////////////////////////
    // affichage de messages d'avancement de la création du shortcut //
    ///////////////////////////////////////////////////////////////////
    private val _shortcutInfoContent = MutableStateFlow<Pair<String, Int>?>(null)
    val shortcutInfoContent = _shortcutInfoContent.asStateFlow()

    fun setShortcutInfoContentToValue(value: String, icon: Int){
        _shortcutInfoContent.update { Pair(value, icon) }
    }

    fun setShortcutInfoContentToNull(){
        _shortcutInfoContent.update { null }
    }

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
                .lastSegment   // ne garde que le nom
                .substringAfterLast('\\')  // ne garde que le nom
                .split('.')
            parts.forEach {
                destinationShortcuts.getOrPut(it) { mutableSetOf() }.add(dir.fullPath)
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
//        currentFileShortcuts.filter { sc -> sc.startsWith('+') }.forEach { sc ->
//            val parts = sc.split('+').filter { it.isNotEmpty() }
//            val firstLevel = parts[0]
//            val totalFirstLevel = "$root/$firstLevel"
//
//            var goodFirstLevel = destinationFolders.value
//                .firstOrNull { folder -> folder.endsWith(firstLevel) }
//
//            var hasGoodFirstLevel = goodFirstLevel != null
//
//            //premier level inconnu
//            if (!hasGoodFirstLevel) {
//                addDestination(totalFirstLevel)
//            }
//
//            val otherParts = parts.drop(1)
//
//            val secondLevels: List<String> = fileRepo.getFolderItems(
//                totalFirstLevel,
//                SortingCriterion.ByNameAsc
//            ).filter { it.isFolder() }.map { it.fullPath }
//
//            val found = secondLevels.firstOrNull { secondLevel ->
//                return@firstOrNull otherParts.all { part ->
//                    secondLevel.contains(part)
//                }
//            }
//
//            //secondLevel pas trouvé : il faut le créer
//            if (found == null)
//                File("$totalFirstLevel/${otherParts.joinToString(".")}").mkdir()
//
//            //shorten
//            val videoNamePartsWithStars =
//                currentFileShortcuts.filter { sc -> sc.startsWith('+') }
//            var modifiedVideoName = video.name
//            videoNamePartsWithStars.forEach { part ->
//                val newPart = part
//                    .split('+')
//                    .filter { subPart -> subPart.isNotEmpty() }
//                    .drop(1).take(1).get(0)
//                Log.d(TAG, "newPart: $newPart")
//
//                modifiedVideoName = modifiedVideoName
//                    .replace(part, newPart)
//            }
//
//            //create shortcuts
//            val correctVideoName = Uri.encode(modifiedVideoName)
//            val destPath = "$totalFirstLevel/${otherParts.joinToString(".")}"
//            val destFullPath = "$destPath/${modifiedVideoName.replace(".mp4", ".html")}"
//            val encodedMp4 = "/videos/$correctVideoName"
////            fileRepo.createShortcut(
////                text(encodedMp4, "bsplayer", coverBitmap, coverBase64),
////                destFullPath.replace(".mp4", " .html")
////            )
//            fileRepo.createShortcut(
//                text(encodedMp4, "vlc", coverBitmap, coverBase64),
//                destFullPath
//            )
//        }
    }

    fun parseVideoName(video: SigmaFile) {
        currentFileShortcuts.clear()
        currentFileShortcuts.addAll(
            video.name
                .substringBeforeLast(".")   //avant .mp4
                .replaceFirst("${video.name.substringBefore(".")}", "")  //après nom principal
                .split('.')
                .filterNot { it.isNullOrEmpty() }
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
    fun getDirectoriesFor(video: ThoFile): Set<SigmaPath> {
        val videoName = video.name
        val shortcuts = videoName
            .substringAfter('.')       //après nom principal
            .substringBeforeLast('.')  //avant .mp4
            .split('.')

        val destinationDirs = mutableSetOf<SigmaPath>()

        for (shortcut in shortcuts) {
            destinationShortcuts[shortcut]?.let { destinationDirs.addAll(it) }
        }
        return destinationDirs
    }


    suspend fun manageHtmlFilesInDestinations(
        copyPictures: Boolean = false,
        rootDir: String,
        onlyOneFileShortcutFile: Item = EmptyItem
    ) {
        Log.d(TAG, "début des générations améliorées")

        setShortcutInfoContentToValue(
            value= "prépa...",
            icon = lorry.folder.items.dossiersigma.R.drawable.chaine)

        val onlyOneFile = onlyOneFileShortcutFile != EmptyItem
        if (onlyOneFile)
            assert(onlyOneFileShortcutFile.picture is String?)

        var createdShortcutsCount = 0

        createDestinationShortcutInventory()
        Log.d(TAG, "dest shortcuts: ${destinationShortcuts.keys.size}")

        var videos = if (onlyOneFile)
            listOf(onlyOneFileShortcutFile)
        else
            ftpDataSource.fetchFiles(storageFolder.value) ?: emptyList()

        Log.d(TAG, "videos: ${videos.size}")

        val imageGroups: MutableMap<SigmaPath, String?> = mutableMapOf()

        val mapFileFullPathToShortcuts: MutableMap<SigmaPath, List<String>> = mutableMapOf()
        videos.forEachDebug { video ->
            mapFileFullPathToShortcuts.put(
                video.fullPath,
                video.fullPath.str.substringBefore(".mp4").substringAfter(".").split(".") + "tous"
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

            imageGroups.put(
                nasVideo.fullPath,
                onlyOneFileShortcutFile.picture as String?
            )
        }

        Log.d(TAG, "après ajouts, imageGroups: ${imageGroups.size}")

        setShortcutInfoContentToValue(
            value= "créations...",
            icon = lorry.folder.items.dossiersigma.R.drawable.chaine)

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
                    val videoFile = SigmaFile(
                        fullPath = nasVideo.fullPath,
                        picture = null,
                        modificationDate = 0L,
                        tag = null,
                        scale = null,
                        memo = null
                    )

                    val retrievedImage = imageGroups[nasVideo.fullPath]
                    if (retrievedImage != null)
                        Log.d(
                            TAG,
                            "création de ${videoFile.fullPath} avec image ${retrievedImage.length}"
                        )
                    else
                        Log.d(TAG, "création de ${videoFile.fullPath} sans image")

                    traiteVideo(
                        videoFile,
                        coverBase64 = retrievedImage,
                        rootDir = rootDir
                    )

                    Log.d(TAG, "${videoFile.name} traité")
                }
        }

        setShortcutInfoContentToValue(
            value= "terminé",
            icon = lorry.folder.items.dossiersigma.R.drawable.chaine)

        delay(1_800)
        setShortcutInfoContentToNull()
    }

//    private suspend fun addDestination(totalFirstLevel: String) {
//=
//        File(totalFirstLevel).mkdir()
//        settingsManager.
//        userPreferences.add_destination_folder(totalFirstLevel)
//    }

    init {
        scope.collectToState(userPreferences.destination_folders.map { it.map { it.toSigmaPath() }.toSet() }, _destinationFolders)
        scope.collectToState(userPreferences.storage_folder.map { it.toSigmaPath() }, _storageFolder)
        Log.d("ShortcutUC", "instance=" + System.identityHashCode(this))
    }
}

suspend fun <T> Iterable<T>.forEachDebug(action: suspend (T) -> Unit) {
    for (element in this) action(element)
}

suspend fun getBase64InHtml(file: Item): String? {
    val htmlFile = file.fullPath.toFile()
    val htmlContent = htmlFile.readText()

    // Regex pour trouver le contenu de src="data:image/...;base64,..."
    val regex = Regex("""<img\s+[^>]*src\s*=\s*"data:image/[^;]+;base64,([^"]+)"""")
    val match = regex.find(htmlContent) ?: return null
    val base64Image = match.groupValues[1]

    return base64Image
}

fun <T> CoroutineScope.collectToState(
    flow: Flow<T>,
    state: MutableStateFlow<T>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    this.launch(dispatcher) {
        flow.collect { value ->
            state.value = value
        }
    }
}

@JvmInline
value class SigmaShortcut(val value: String)
