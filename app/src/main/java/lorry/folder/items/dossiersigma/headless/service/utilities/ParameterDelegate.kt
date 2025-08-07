package lorry.folder.items.dossiersigma.headless.service.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ParameterDelegate<T : Any>(
    val type: KClass<T>
) : ReadWriteProperty<Any?, T> {
    var value: T? = null

    fun assignFromString(raw: String) {
        @Suppress("UNCHECKED_CAST")
        value = when (type) {
            Int::class -> raw.toInt() as T
            Boolean::class -> raw.toBoolean() as T
            String::class -> raw as T
            List::class -> {
                val typeToken = object : TypeToken<List<String>>() {}.type
                Gson().fromJson<List<String>>(raw, typeToken) as T
            }
            else -> error("Unsupported type: $type")
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: error("Parameter '${property.name}' not set.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

inline fun <reified T : Any> parameter() = ParameterDelegate(T::class)