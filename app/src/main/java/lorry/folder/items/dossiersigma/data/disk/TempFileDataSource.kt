package lorry.folder.items.dossiersigma.data.disk

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class TempFileDataSource @Inject constructor(): ITempFileDataSource {
    override suspend fun saveUrlToTempFile(fileUrl: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val inputStream = response.body?.byteStream()
                inputStream?.let {
                    val tempFile = File.createTempFile("image", ".jpg")
                    FileOutputStream(tempFile).use { output ->
                        it.copyTo(output)
                    }
                    tempFile.absolutePath
                }
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}