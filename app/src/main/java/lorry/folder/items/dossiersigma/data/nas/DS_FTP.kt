package lorry.folder.items.copieurtho2.__data.NAS

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.nas.DSI_FTP
import lorry.folder.items.dossiersigma.domain.usecases.homePage.SettingDatas
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPClientConfig
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DS_FTP @Inject constructor(
    val settingsManager: SettingsManager
) : DSI_FTP {

    suspend fun <T : Any?> doWithNASAccess(
        parent: String,
        doWithFtpClient: suspend (FTPClient) -> Result<T?>
    ): T? {

        val ftp: FTPClient = FTPClient()
        val config: FTPClientConfig = FTPClientConfig()
        config.setServerTimeZoneId("Europe/Paris")
        ftp.configure(config)

        var answer: T? = null

        try {
            val server = settingsManager.nasAddressFlow.firstOrNull()
            withContext(Dispatchers.IO) {
                ftp.connect(server)
            }

            val reply = ftp.getReplyCode()
            if (!FTPReply.isPositiveCompletion(reply)) {
                withContext(Dispatchers.IO) {
                    ftp.disconnect()
                }
                println("FTP server refused connection.")
                throw Exception("FTP server refused connection.")
            }

            val login = settingsManager.nasLoginFlow.firstOrNull()
            val password = settingsManager.nasPasswordFlow.firstOrNull()

            Log.d("SIGMA DISK", "connexion: server: $server, login: $login, password: $password")

            val connected = withContext(Dispatchers.IO) {
                ftp.login(login, password)
            }
            if (!connected) {
                println("Login failed")
                throw Exception("Login failed")
            }

            val result = withContext(Dispatchers.IO) {
                doWithFtpClient(ftp)
            }
            if (result.isSuccess)
                answer = result.getOrNull()
            else answer = null

            withContext(Dispatchers.IO) {
                ftp.logout()
            }
        } catch (ex: Exception) {
            println("erreur: ${ex.message}")
        } finally {
            if (ftp.isConnected) {
                try {
                    withContext(Dispatchers.IO) {
                        ftp.disconnect()
                    }
                } catch (ex: Exception) {
                }
            }

        }

        return answer
    }

    override suspend fun fetchDirectories(parent: String): List<String>? {
        return doWithNASAccess(parent) { ftp ->
            val liste = withContext(Dispatchers.IO) {
                ftp.listDirectories(parent)
                    .filter { entry -> entry.isDirectory }
                    .map { file -> "/${file.name}" }
            }

            Result.success(liste)
        }
    }

    override suspend fun fetchMP4Files(parent: String): List<ThoFile>? {
        return doWithNASAccess(parent) { ftp ->
            val liste = withContext(Dispatchers.IO) {
                ftp.listFiles(parent)
                    ?.filter { file ->
                        file.name.endsWith(".mp4") ||
                                file.name.endsWith(".mpg") ||
                                file.name.endsWith(".mkv") ||
                                file.name.endsWith(".avi") ||
                                file.name.endsWith(".ts") ||
                                file.name.endsWith(".iso")
                    }
                    ?.map { file ->
                        ThoFile(
                            name = file.name,
                            timestamp = file.timestamp,
                            size = file.size,
                            fullPath = Paths.get(parent, file.name).toString(),
                            isVideoFile = true,
                            isHtmlFile = false,
                            null
                        )
                    }
            }
            Result.success(liste)
        }
    }

    override suspend fun fetchMP4File(parent: String): List<ThoFile>? {
        return doWithNASAccess(parent) { ftp ->
            val liste = withContext(Dispatchers.IO) {
                ftp.listFiles(parent)
                    ?.filter { file -> file.name.endsWith(".mp4") }
                    ?.map { file ->
                        ThoFile(
                            name = file.name,
                            timestamp = file.timestamp,
                            size = file.size,
                            fullPath = Paths.get(parent, file.name).toString(),
                            isVideoFile = true,
                            isHtmlFile = false,
                            null
                        )
                    }
            }

            Result.success(liste)
        }
    }

    override suspend fun fetchHtmlFiles(
        parent: String,
        display: (suspend (String) -> Unit)?
    ): List<ThoFile>? {
        val liste0 = doWithNASAccess<List<FTPFile>?>(parent) { ftp ->
            val files = withContext(Dispatchers.IO) {
                ftp.listFiles(parent)
            }
                ?.filter { file ->
                    file.name.endsWith(".html")
                }
            Result.success(files)
        }

        val total = liste0?.size ?: 0
        val liste = liste0?.mapIndexed { n, file ->
            display?.invoke("image de fichier ${n + 1}/$total: ${file.name}")
            ThoFile(
                name = file.name,
                timestamp = file.timestamp,
                size = file.size,
                fullPath = Paths.get(parent, file.name).toString(),
                isVideoFile = false,
                isHtmlFile = true,
                pictureBase64 = getBase64InHtml(parent, file)
            )
        }

        return liste
    }


    suspend fun getBase64InHtml(parent: String, htmlFile: FTPFile): String? {
        var htmlContent = doWithNASAccess<String?>(parent) { ftp ->
            var htmlResult: String? = null
            val changed = ftp.changeWorkingDirectory(parent)

            if (changed != true) {
                println("❌ Impossible de se positionner sur le dossier : $parent")
                return@doWithNASAccess Result.failure<String?>(Exception("Impossible de se positionner sur le dossier : $parent"))
            }

            val inputStream = ftp.retrieveFileStream(htmlFile.name)

            if (inputStream == null) {
                println("❌ Impossible de récupérer le fichier : ${htmlFile.name}")
                return@doWithNASAccess Result.failure<String?>(Exception("Impossible de récupérer le fichier : ${htmlFile.name}"))
            }
            val html = inputStream.bufferedReader().use { reader ->
                reader.readText()
            }

            // 🔥 Très important : on complète la commande
            val success = ftp.completePendingCommand()


            if (success == true) {
                htmlResult = html
            } else {
                println("FTP: Échec de la lecture de ${htmlFile.name}")
                println("Lecture incomplète du fichier FTP")
                return@doWithNASAccess Result.failure<String?>(Exception("Lecture incomplète du fichier FTP"))
            }

            return@doWithNASAccess Result.success(htmlResult)
        }


        //htmlFile.readText()

        if (htmlContent == null)
            return ""

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<img\s+[^>]*src\s*=\s*"data:image/[^;]+;base64,([^"]+)"""")
        val match = regex.find(htmlContent) ?: return null
        val base64Image = match.groupValues[1]

        return base64Image
    }

    override suspend fun fetchUniqueLevel2Files(parent: String): List<ThoFile>? {
        return doWithNASAccess(parent) { ftp ->
            val liste = withContext(Dispatchers.IO) {
                ftp.listFiles(parent)
                    ?.filter { file ->
                        file.name.endsWith(".html") || file.name.endsWith(".mp4")
                                || file.name.endsWith(".mkv") || file.name.endsWith(".ts")
                    }
                    ?.map<FTPFile, ThoFile> { file ->
                        ThoFile(
                            name = file.name,
                            timestamp = file.timestamp,
                            size = file.size,
                            fullPath = Paths.get(parent, file.name).toString(),
                            isVideoFile = file.name.endsWith(".mp4")
                                    || file.name.endsWith(".mkv") || file.name.endsWith(".ts"),
                            isHtmlFile = file.name.endsWith(".html"),
                            null
                        )
                    }?.distinctBy { file ->
                        file.name
                            .replace(" .html", ".html").replace(" .mp4", ".mp4")
                            .replace(" .mkv", ".mkv").replace(" .ts", ".ts")
                    }
            }

            Result.success(liste)
        }
    }

    override suspend fun createDirectory(parent: String, child: String): Boolean? {
        return doWithNASAccess(parent) { ftp ->
            val result = withContext(Dispatchers.IO) {
                ftp.changeWorkingDirectory(parent)
            }
            if (!result) {
                println("Échec du changement de répertoire: $parent")
                return@doWithNASAccess Result.failure<Boolean>(Exception("Répertoire introuvable sur le NAS"))
            }

            if (ftp.makeDirectory(child))
                Result.success(true)
            else
                Result.failure(Exception("répertoire non créé"))
        }
    }

    override suspend fun copy(
        file: ThoFile,
        pathOnNAS: String,
        progressCallback: (Int) -> Unit // Ajout du callback pour la progression
    ): Boolean {
        return doWithNASAccess<Boolean>(parent = pathOnNAS) { ftp ->
            val localFilePath = file.fullPath
            val remoteFilePath = "$pathOnNAS/${file.name}"

            try {
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE)

                val result = withContext(Dispatchers.IO) {
                    ftp.changeWorkingDirectory(pathOnNAS)
                }

                if (!result) {
                    println("Échec du changement de répertoire: $pathOnNAS")
                    return@doWithNASAccess Result.failure<Boolean>(Exception("Répertoire introuvable sur le NAS"))
                }

                val fileToUpload = java.io.File(localFilePath)
                val fileSize = fileToUpload.length() // Taille totale du fichier
                val buffer = ByteArray(4096) // Taille du buffer
                var uploadedSize = 0L

                fileToUpload.inputStream().use { inputStream ->
                    withContext(Dispatchers.IO) {
                        ftp.storeFileStream(file.name)?.use { outputStream ->
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                uploadedSize += bytesRead
                                val progress = (uploadedSize * 100 / fileSize).toInt()
                                progressCallback(progress) // Notifier la progression
                            }
                        }
                    }
                }

                if (ftp.completePendingCommand()) {
                    println("Fichier copié avec succès: $remoteFilePath")
                    Result.success(true)
                } else {
                    println("Échec de la copie du fichier: $remoteFilePath")
                    Result.failure(Exception("Échec de la copie"))
                }
            } catch (ex: Exception) {
                println("Erreur lors de la copie du fichier: ${ex.message}")
                Result.failure(ex)
            }
        } == true
    }

    override suspend fun copy(
        localFilePath: String,
        pathOnNAS: String,
        progressCallback: (Int) -> Unit // Ajout du callback pour la progression
    ): Boolean {
        return doWithNASAccess<Boolean>(parent = pathOnNAS) { ftp ->
            val remoteFilePath = "$pathOnNAS/${localFilePath.substringAfterLast("/")}"

            try {
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE)

                val result = withContext(Dispatchers.IO) {
                    ftp.changeWorkingDirectory(pathOnNAS)
                }

                if (!result) {
                    println("Échec du changement de répertoire: $pathOnNAS")
                    return@doWithNASAccess Result.failure<Boolean>(Exception("Répertoire introuvable sur le NAS"))
                }

                val fileToUpload = java.io.File(localFilePath)
                val fileSize = fileToUpload.length() // Taille totale du fichier
                val buffer = ByteArray(4096) // Taille du buffer
                var uploadedSize = 0L

                fileToUpload.inputStream().use { inputStream ->
                    withContext(Dispatchers.IO) {
                        ftp.storeFileStream(localFilePath.substringAfterLast("/"))
                            ?.use { outputStream ->
                                var bytesRead: Int
                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                    uploadedSize += bytesRead
                                    val progress = (uploadedSize * 100 / fileSize).toInt()
                                    progressCallback(progress) // Notifier la progression
                                }
                            }
                    }
                }

                if (ftp.completePendingCommand()) {
                    println("Fichier copié avec succès: $remoteFilePath")
                    Result.success(true)
                } else {
                    println("Échec de la copie du fichier: $remoteFilePath")
                    Result.failure(Exception("Échec de la copie"))
                }
            } catch (ex: Exception) {
                println("Erreur lors de la copie du fichier: ${ex.message}")
                Result.failure(ex)
            }
        } == true
    }

    suspend fun copy0(file: ThoFile, pathOnNAS: String): Boolean {
        return doWithNASAccess<Boolean>(parent = pathOnNAS) { ftp ->
            val localFilePath = file.fullPath
            val remoteFilePath = "$pathOnNAS/${file.name}"

            try {
                // Activer le mode binaire
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE)

                ftp.changeWorkingDirectory(pathOnNAS)
                // Changer de répertoire
                if (ftp.printWorkingDirectory() != pathOnNAS) {
//                    wd = ftp.getReplyString()
                    println("Échec du changement de répertoire: $pathOnNAS")
                    return@doWithNASAccess Result.failure<Boolean>(Exception("Répertoire introuvable sur le NAS"))
                }

                // Ouvrir un flux pour lire le fichier local
                val inputStream = java.io.File(localFilePath).inputStream()

                // Transférer le fichier
                val success = ftp.storeFile(file.name, inputStream)
                inputStream.close()

                if (success) {
                    println("Fichier copié avec succès: $remoteFilePath")
                    Result.success(true)
                } else {
                    println("Échec de la copie du fichier: $remoteFilePath")
                    Result.failure(Exception("Échec de la copie"))
                }
            } catch (ex: Exception) {
                println("Erreur lors de la copie du fichier: ${ex.message}")
                Result.failure(ex)
            }
        } == true
    }

    override suspend fun shorten(
        file: ThoFile,
        pathOnNAS: String
    ): String? {
        return doWithNASAccess<String?>(parent = pathOnNAS) { ftp ->
            val remoteFilePath = "$pathOnNAS/${file.name}"

            val videoNamePartsWithStars = file.name.split('.').filter { it != "" }
                .filter { sc -> sc.startsWith('+') }
            var modifiedVideoName = file.name
            videoNamePartsWithStars.forEach { part ->
                val newPart = part
                    .split('+')
                    .filter { subPart -> subPart.isNotEmpty() }
                    .drop(1).take(1)[0]

                modifiedVideoName = modifiedVideoName
                    .replace(part, newPart)
            }
            //val newName = "${file.name.substringBefore('.')}.mp4"

            val newPath = Paths.get(pathOnNAS, modifiedVideoName).toString()
            val success = withContext(Dispatchers.IO) {
                ftp.rename(remoteFilePath, newPath)
            }

            when {
                success -> return@doWithNASAccess Result.success(modifiedVideoName)
                else -> return@doWithNASAccess Result.failure(Exception())
            }
        }
    }

    override suspend fun delete(
        fileFullPath: String
    ): Boolean {
        return doWithNASAccess<Boolean>(parent = fileFullPath) { ftp ->
            val success = withContext(Dispatchers.IO) {
                ftp.deleteFile(fileFullPath)
            }

            if (success)
                return@doWithNASAccess Result.success(true)
            else
                return@doWithNASAccess Result.failure(Exception())
        } == true
    }

    override suspend fun createShortcut(text: String, fullPathOnNAS: String): Boolean {
        return doWithNASAccess<Boolean>(parent = fullPathOnNAS) { ftp ->

            val pathOnNAS = fullPathOnNAS.substringBeforeLast('/') + "/"

            try {
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE)

                val result = withContext(Dispatchers.IO) {
                    ftp.changeWorkingDirectory(pathOnNAS)
                }
                if (!result) {
                    println("Échec du changement de répertoire: $pathOnNAS")
                    return@doWithNASAccess Result.failure<Boolean>(Exception("Répertoire introuvable sur le NAS"))
                }

                val fileName = fullPathOnNAS.substringAfterLast("/").replace(".mp4", ".html")

                val inputStream =
                    ByteArrayInputStream(text.trimIndent().toByteArray(StandardCharsets.UTF_8))

                val success = withContext(Dispatchers.IO) {
                    ftp.storeFile(fileName, inputStream)
                }

                inputStream.close()

                if (success) {
                    println("Fichier HTML créé avec succès: $fullPathOnNAS")
                    Result.success(true)
                } else {
                    println("Échec de la création du fichier HTML: $fullPathOnNAS")
                    Result.failure(Exception("Échec de la création du fichier"))
                }

            } catch (ex: Exception) {
                println("Erreur lors de la création du fichier HTML: ${ex.message}")
                Result.failure(ex)
            }

        } == true
    }

    suspend fun createPath(path: String): Boolean {
        return doWithNASAccess<Boolean>(parent = path) { ftp ->
            try {
                // Découper le chemin pour créer chaque dossier manquant un par un
                val directories = path.trim('/').split('/')
                var currentPath = ""

                for (dir in directories) {
                    currentPath += "/$dir"

                    // Vérifier si le répertoire existe déjà
                    if (!ftp.changeWorkingDirectory(currentPath)) {
                        val success = withContext(Dispatchers.IO) {
                            ftp.makeDirectory(currentPath)
                        }

                        if (!success) {
                            println("Échec de la création du répertoire : $currentPath")
                            return@doWithNASAccess Result.failure<Boolean>(Exception("Impossible de créer le répertoire $currentPath"))
                        } else {
                            println("Répertoire créé avec succès : $currentPath")
                        }
                    }
                }

                Result.success(true)
            } catch (ex: Exception) {
                println("Erreur lors de la création du répertoire : ${ex.message}")
                Result.failure(ex)
            }
        } == true

    }
}

data class ThoFile(
    val name: String,
    val timestamp: Calendar,
    val size: Long,
    val fullPath: String,
    val isVideoFile: Boolean,
    val isHtmlFile: Boolean,
    val pictureBase64: String?
) {
    companion object {
        val EMPTY = ThoFile("", Calendar.getInstance(), 0, "", false, false, null)

    }
}