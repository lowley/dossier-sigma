package lorry.folder.items.dossiersigma.data.dataSaver

import com.google.gson.Gson
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset
import javax.inject.Inject

class FileCompositeIO @Inject constructor() {

    private val CHARSET = "UTF-8"
    
    private val START_COMPOSITE = "##SIGMA-COMPOSITE-START##"

    //ici il y aura le composite
    private val END_COMPOSITE = "##SIGMA-COMPOSITE-END##"
    private val START_LENGTH = "##SIGMA-LENGTH-START##"
    private val END_LENGTH = "##SIGMA-LENGTH-END##"
    

    suspend fun getComposite(filePath: String): CompositeData? {

        val file = File(filePath)
        val charset = Charset.forName(CHARSET)
        var extractedText: String? = null

        try {
            RandomAccessFile(file, "r").use { raf ->
                val length = raf.length()
                val (tail, startIndex, endIndex) = tryToExtract(
                    length, raf, charset)

                if (tail == null) {
                    extractedText = null
                    return@use
                }

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    extractedText = tail.substring(startIndex + START_COMPOSITE.length, endIndex).trim()
                }
            }

            //composite pas trouvé malgré tous les essais
            
        } catch (ex: Exception) {
            println("erreur getComposite: ${ex.message}")
            extractedText = null
        }

        if (extractedText == null)
            return null
        
        val extractedComposite =
            Gson().fromJson<CompositeData>(extractedText, CompositeData::class.java)

        return extractedComposite
    }

    suspend fun saveComposite(
        filePath: String,
        composite: CompositeData
    ): Boolean {
        val file = File(filePath)
        if (!file.exists())
            return false

        if (getComposite(filePath) != null)
            removeCompositeAndDatas(filePath)

        val data = Gson().toJson(composite)
        saveData(filePath, data = data, START_COMPOSITE, END_COMPOSITE)

        val dataLength = data.length.toString()
        saveData(filePath, data = dataLength, START_LENGTH, END_LENGTH)

        val verification = getComposite(filePath)
        return verification == composite
    }

    /**
     * ajoute data à la fin du fichier, entouré des balises "start" et "end"
     */
    private fun saveData(filePath: String, data: String, start: String, end: String) {
        val file = File(filePath)
        if (!file.exists())
            return

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(raf.length())
            val insert = "\n$start\n$data\n$end\n"
            raf.write(insert.toByteArray(Charset.forName(CHARSET)))
        }
    }

    suspend fun removeCompositeAndDatas(
        filePath: String,
    ) {
        ///////////////////////
        // lecture de length //
        ///////////////////////
        val file = File(filePath)
        val length = file.length()
        val charset = Charset.forName(CHARSET)

        RandomAccessFile(file, "rw").use { raf ->
            val firstSeekLength = minOf(10000.toLong(), length)
            val firstSeekStart = minOf(length, firstSeekLength)
            //position de départ
            raf.seek(firstSeekStart)
            //ce qui est lu cette fois-ci, au max longueur initialLookbackBytes
            val firstSeekBytes = ByteArray(firstSeekLength.toInt())
            //lecture jusqu'à gaver bytes
            raf.readFully(firstSeekBytes)

            //lecture des bytes et recherche
            val firstSeekTail = String(firstSeekBytes, charset)
            val firstSeekStartIndex = firstSeekTail.lastIndexOf(START_LENGTH)
            val firstSeekEndIndex = firstSeekTail.lastIndexOf(END_LENGTH)

            val firstSeekResult =
                if (firstSeekStartIndex != -1 && firstSeekEndIndex != -1 && firstSeekEndIndex > firstSeekStartIndex)
                    firstSeekTail.substring(firstSeekStartIndex + START_LENGTH.length, firstSeekEndIndex)
                        .trim().toLong()
                else 0L
            println("recherche length: $firstSeekResult")

            //////////////////////////
            // lecture du composite //
            //////////////////////////
            val firstSeekSTART_LENGTHtoEnd = firstSeekLength - firstSeekStartIndex
            val secondSeekEnd = length - firstSeekSTART_LENGTHtoEnd
            //longueurs des contenus des variables START_COMPOSITE + END_COMPOSITE = 48
            val secondSeekStart = secondSeekEnd - firstSeekResult - 50
            //position de départ
            raf.seek(secondSeekStart)
            //ce qui est lu cette fois-ci, au max longueur initialLookbackBytes
            val secondSeekBytes = ByteArray(firstSeekResult.toInt())
            //lecture jusqu'à gaver bytes
            raf.readFully(secondSeekBytes)

            //lecture des bytes et recherche
            val secondSeekTail = String(secondSeekBytes, charset)
            val secondSeekStartIndex = firstSeekTail.lastIndexOf(START_COMPOSITE)
            val secondSeekEndIndex = firstSeekTail.lastIndexOf(END_COMPOSITE)
            
            if (secondSeekStartIndex != -1 && secondSeekEndIndex != -1 && secondSeekEndIndex > secondSeekStartIndex) {
                raf.setLength(secondSeekStart + secondSeekEndIndex)
            }
        }
    }

    suspend private fun tryToExtract(
        length: Long,
        raf: RandomAccessFile,
        charset: Charset,
    ): Triple<String?, Int, Int> {

        ///////////////////////
        // lecture de length //
        ///////////////////////
        val firstSeekLength = minOf(10000.toLong(), length)
        val firstSeekStart = minOf(length, firstSeekLength)
        //position de départ
        raf.seek(firstSeekStart)
        //ce qui est lu cette fois-ci, au max longueur initialLookbackBytes
        val firstSeekBytes = ByteArray(firstSeekLength.toInt())
        //lecture jusqu'à gaver bytes
        raf.readFully(firstSeekBytes)

        //lecture des bytes et recherche
        val firstSeekTail = String(firstSeekBytes, charset)
        val firstSeekStartIndex = firstSeekTail.lastIndexOf(START_LENGTH)
        val firstSeekEndIndex = firstSeekTail.lastIndexOf(END_LENGTH)

        val firstSeekResult =
            if (firstSeekStartIndex != -1 && firstSeekEndIndex != -1 && firstSeekEndIndex > firstSeekStartIndex)
                firstSeekTail.substring(firstSeekStartIndex + START_LENGTH.length, firstSeekEndIndex).trim()
                    .toLong()
            else 0L
        println("recherche length: $firstSeekResult")

        //////////////////////////
        // lecture du composite //
        //////////////////////////
        val firstSeekSTART_LENGTHtoEnd = firstSeekLength - firstSeekStartIndex
        val secondSeekEnd = length - firstSeekSTART_LENGTHtoEnd
        //longueurs des contenus des variables START_COMPOSITE + END_COMPOSITE = 48
        val secondSeekStart = secondSeekEnd - firstSeekResult - 50
        //position de départ
        raf.seek(secondSeekStart)
        //ce qui est lu cette fois-ci, au max longueur initialLookbackBytes
        val secondSeekBytes = ByteArray(firstSeekResult.toInt())
        //lecture jusqu'à gaver bytes
        raf.readFully(secondSeekBytes)

        //lecture des bytes et recherche
        val secondSeekTail = String(secondSeekBytes, charset)
        val secondSeekStartIndex = firstSeekTail.lastIndexOf(START_COMPOSITE)
        val secondSeekEndIndex = firstSeekTail.lastIndexOf(END_COMPOSITE)

        val result =
            if (secondSeekStartIndex != -1 && secondSeekEndIndex != -1 && secondSeekEndIndex > secondSeekStartIndex)
                secondSeekTail.substring(secondSeekStartIndex + START_COMPOSITE.length, secondSeekEndIndex)
                    .trim()
            else null

        return Triple(result, secondSeekStartIndex, secondSeekEndIndex)
    }
}