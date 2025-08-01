package lorry.folder.items.dossiersigma.ui.settings

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.kavi.droid.color.palette.extension.base
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemInfos
import lorry.folder.items.dossiersigma.ui.bottomArea.HomeItemInfosDTO
import javax.inject.Inject
import javax.inject.Singleton

// On déclare la classe comme un Singleton pour n'avoir qu'une seule instance dans toute l'app
@Singleton
class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {

    // Crée une instance de DataStore liée à un fichier "settings.preferences_pb"
    // Le nom est arbitraire mais doit être unique.
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    // On définit les clés pour chaque valeur que l'on veut stocker.
    // C'est une bonne pratique de les déclarer comme des objets compagnons.
    companion object {
        val NAS_ADDRESS_KEY = stringPreferencesKey("nas_address")
        val NAS_LOGIN_KEY = stringPreferencesKey("nas_login")
        val NAS_PASSWORD_KEY = stringPreferencesKey("nas_password")
        val NAS_FOLDER_KEY = stringPreferencesKey("nas_folder")
        val HOMEITEMS_KEY = stringSetPreferencesKey("home_items")
        val THEME_KEY = stringSetPreferencesKey("theme_colors")
    }

    suspend fun saveNasAddress(address: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_ADDRESS_KEY] = address
            }
        }
    }

    val nasAddressFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // On lit la valeur associée à notre clé.
            // Si elle n'existe pas, on retourne une valeur par défaut (chaîne vide).
            preferences[NAS_ADDRESS_KEY] ?: ""
        }

    suspend fun saveNasLogin(login: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_LOGIN_KEY] = login
            }
        }
    }

    val nasLoginFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[NAS_LOGIN_KEY] ?: ""
        }

    suspend fun saveNasPassword(password: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_PASSWORD_KEY] = password
            }
        }
    }

    val nasPasswordFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // On lit la valeur associée à notre clé.
            // Si elle n'existe pas, on retourne une valeur par défaut (chaîne vide).
            preferences[NAS_PASSWORD_KEY] ?: ""
        }

    suspend fun saveNasFolder(folder: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[NAS_FOLDER_KEY] = folder
            }
        }
    }

    val nasFolderFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // On lit la valeur associée à notre clé.
            // Si elle n'existe pas, on retourne une valeur par défaut (chaîne vide).
            preferences[NAS_FOLDER_KEY] ?: ""
        }

    suspend fun saveHomeItems(items: Set<HomeItemInfos>) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[HOMEITEMS_KEY] =
                    items.map { Gson().toJson(it.toHomeItemInfosDTO()) }.toSet()
            }
        }
    }

    val homeItemsFlow: Flow<List<HomeItemInfos>> = context.dataStore.data
        .map { preferences ->
            val raw = preferences[HOMEITEMS_KEY] ?: return@map emptyList()

            val cool = raw.map {
                Gson().fromJson(it, HomeItemInfosDTO::class.java)
                    .toHomeItemInfos()
            }
                .sortedBy { it.index }
                .mapIndexed { index, homeItemInfos ->
                    homeItemInfos.copy(index = index)
                }

            return@map cool
        }

    suspend fun saveColorScheme(scheme: ColorScheme) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                settings[THEME_KEY] = setOf(
                    scheme.primary.toPersistableString("primary"),
                    scheme.onPrimary.toPersistableString("onprimary"),
                    scheme.secondary.toPersistableString("secondary"),
                    scheme.onSecondary.toPersistableString("onsecondary"),
                    scheme.tertiary.toPersistableString("terciary"),
                    scheme.onTertiary.toPersistableString("onterciary"),
                    scheme.background.toPersistableString("background"),
                    scheme.base.toPersistableString("base"),
                ).toSet()
            }
        }
    }

    val colorSchemeFlow: Flow<MyColorScheme> = context.dataStore.data
        .map { preferences ->
            val rawColors = preferences[THEME_KEY]
            if (rawColors == null)
                return@map MyColorScheme()

            return@map MyColorScheme(
                primary = rawColors.getColor("primary"),
                onPrimary = rawColors.getColor("onprimary"),
                secondary = rawColors.getColor("secondary"),
                onSecondary = rawColors.getColor("onsecondary"),
                tertiary = rawColors.getColor("terciary"),
                onTertiary = rawColors.getColor("onterciary"),
                background = rawColors.getColor("background"),
                base = rawColors.getColor("base"),
            )
        }

    fun Set<String>.getColor(key: String): Color {
        val result = this.firstOrNull {
            it.startsWith(key)
        }?.substringAfter("|")?.let {
            Color(it.removePrefix("0x").toULong(16))
        }?: Color.Black

        return result
    }
}

data class MyColorScheme(
    val primary : Color = Color.White,
    val onPrimary : Color = Color.DarkGray,
    val secondary : Color = Color.Green,
    val onSecondary : Color = Color.LightGray,
    val tertiary : Color = Color.Magenta,
    val onTertiary : Color = Color.Cyan,
    val background : Color = Color.Black,
    val base : Color = Color.LightGray,
)

fun ColorScheme.toMyColorScheme(): MyColorScheme{
    return MyColorScheme(
        primary = this.primary,
        onPrimary = this.onPrimary,
        secondary = this.secondary,
        onSecondary = this.onSecondary,
        tertiary = this.tertiary,
        onTertiary = this.onTertiary,
        background = this.background,
        base = this.base,
    )
}

fun MyColorScheme.toColorScheme(): ColorScheme {
    val base = this.base
    val primary = this.primary
    val secondary = this.secondary
    val tertiary = this.tertiary

    return ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.8f),
        onPrimaryContainer = onPrimary,
        inversePrimary = tertiary, // Accent inversé

        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondary.copy(alpha = 0.85f),
        onSecondaryContainer = onSecondary,

        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiary.copy(alpha = 0.85f),
        onTertiaryContainer = onTertiary,

        background = primary, // principe 60% : couleur de fond
        onBackground = background, // texte sur fond principal

        surface = secondary.copy(alpha = 0.9f), // zones sur fond
        onSurface = onSecondary,

        surfaceVariant = secondary.copy(alpha = 0.6f),
        onSurfaceVariant = onSecondary,

        surfaceTint = primary.copy(alpha = 0.4f),
        inverseSurface = base.copy(alpha = 0.9f),
        inverseOnSurface = Color.Black,

        error = Color(0xFFB00020),
        onError = Color.White,
        errorContainer = Color(0xFFCF6679),
        onErrorContainer = Color.Black,

        outline = base.copy(alpha = 0.6f),
        outlineVariant = base.copy(alpha = 0.4f),
        scrim = Color(0x66000000),

        surfaceBright = base.copy(alpha = 0.1f),
        surfaceDim = base.copy(alpha = 0.85f),

        surfaceContainer = base.copy(alpha = 0.1f),
        surfaceContainerHigh = base.copy(alpha = 0.2f),
        surfaceContainerHighest = base.copy(alpha = 0.3f),
        surfaceContainerLow = base.copy(alpha = 0.05f),
        surfaceContainerLowest = Color.Transparent
    )
}


fun Color.toPersistableString(colorName: String) =
    "$colorName|${this.toHex().take(10)}"

fun Color.toHex(): String = "0x%08X".format(this.value.toLong())

@Composable
fun GetMyAppTheme(
    colorScheme: MyColorScheme
): @Composable ((@Composable () -> Unit) -> Unit) {
    return { content ->
        MaterialTheme(
            colorScheme = colorScheme.toColorScheme(),
            content = content
        )
    }
}
