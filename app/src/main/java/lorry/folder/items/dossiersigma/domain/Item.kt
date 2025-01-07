package lorry.folder.items.dossiersigma.domain

import android.graphics.Bitmap
import java.util.UUID

data class Item(
    val name: String,
    val isFile: Boolean,
    val content: Bitmap?,
    val id: String = UUID.randomUUID().toString()) { }

