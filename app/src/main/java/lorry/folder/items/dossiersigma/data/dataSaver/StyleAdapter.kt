package lorry.folder.items.dossiersigma.data.dataSaver

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.pointlessapps.rt_editor.model.Style
import kotlin.jvm.java

class StyleAdapter : TypeAdapter<Style>() {

    private val gson = Gson()

    // Cette fonction écrit l'objet Style en JSON
    override fun write(out: JsonWriter, value: Style?) {
        if (value == null) {
            out.nullValue()
            return
        }
        // Convertit l'objet (ex: TextColor) en JSON de base
        val jsonObject = gson.toJsonTree(value).asJsonObject

        // Ajoute le champ "type" en utilisant le nom de la classe (ex: "TextColor")
        jsonObject.addProperty("type", value::class.java.simpleName)

        // Écrit l'objet JSON final
        gson.toJson(jsonObject, out)
    }

    // Cette fonction lit le JSON pour recréer un objet Style
    // Dans votre classe StyleAdapter

    override fun read(reader: JsonReader): Style? {
        val jsonObject = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
        if (jsonObject == null || !jsonObject.has("type")) {
            return null
        }

        val typeName = jsonObject.get("type").asString

        // On retourne directement le résultat de la désérialisation depuis le "when"
        return when (typeName) {
            "TextColor" -> gson.fromJson(jsonObject, Style.TextColor::class.java)
            "AlignLeft" -> gson.fromJson(jsonObject, Style.AlignLeft::class.java)
            "AlignCenter" -> gson.fromJson(jsonObject, Style.AlignCenter::class.java)
            "AlignRight" -> gson.fromJson(jsonObject, Style.AlignRight::class.java)
            "Bold" -> gson.fromJson(jsonObject, Style.Bold::class.java)
            "Italic" -> gson.fromJson(jsonObject, Style.Italic::class.java)
            "Underline" -> gson.fromJson(jsonObject, Style.Underline::class.java)
            "Strikethrough" -> gson.fromJson(jsonObject, Style.Strikethrough::class.java)
            "ParagraphStyle" -> gson.fromJson(jsonObject, Style.ParagraphStyle::class.java)
            "OrderedList" -> gson.fromJson(jsonObject, Style.OrderedList::class.java)
            "UnorderedList" -> gson.fromJson(jsonObject, Style.UnorderedList::class.java)
            "TextStyle" -> gson.fromJson(jsonObject, Style.TextStyle::class.java)
            "TextSize" -> gson.fromJson(jsonObject, Style.TextSize::class.java)
            // Ajoutez d'autres types ici
            else -> throw IllegalArgumentException("Unknown style type: $typeName")
        }
    }
}