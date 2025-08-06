package lorry.folder.items.copieurtho2.__data.NAS

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.exposure.nas.DSI_FTP
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPClientConfig
import org.apache.commons.net.ftp.FTPReply
import java.nio.file.Paths
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

    override suspend fun fetchFiles(parent: String): List<SigmaFile>? {
        return doWithNASAccess(parent) { ftp ->
            val liste = withContext(Dispatchers.IO) {
                ftp.listFiles(parent)
//                    ?.filter { file ->
//                        file.name.endsWith(".mp4") ||
//                                file.name.endsWith(".mpg") ||
//                                file.name.endsWith(".mkv") ||
//                                file.name.endsWith(".avi") ||
//                                file.name.endsWith(".ts") ||
//                                file.name.endsWith(".iso")
//                    }
                    ?.map { file ->
                        SigmaFile(
                            name = file.name,
                            modificationDate = file.timestamp.timeInMillis,
                            path = Paths.get(parent, file.name).toString(),
                            picture = null,
                            tag = null,
                            scale = null,
                            memo = null
                        )
                    }
            }
            Result.success(liste)
        }
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

}