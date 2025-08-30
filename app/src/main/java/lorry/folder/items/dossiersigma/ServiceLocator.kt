package lorry.folder.items.dossiersigma

import android.content.Context
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.external.nas.DS_FTP
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.MoveEngine
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities

object ServiceLocator {

    // --- DS_FTP (singleton dépendant de Settings) ----------------------------
    @Volatile private var dsFtpSingleton: DSI_FTP? = null
    fun dsFtp(ctx: Context): DSI_FTP =
        dsFtpSingleton ?: synchronized(this) {
            dsFtpSingleton ?: DS_FTP(ctx).also { dsFtpSingleton = it }
        }

    // --- NasUtilities (singleton) -------------------------------------------
    @Volatile private var nasUtilitiesSingleton: NasUtilities? = null
    fun nasUtilities(ctx: Context): NasUtilities =
        nasUtilitiesSingleton ?: synchronized(this) {
            nasUtilitiesSingleton ?: NasUtilities(dsFtp(ctx)).also { nasUtilitiesSingleton = it }
        }

    // --- MoveEngine (factory légère : stateless, on peut recréer si besoin) --
    fun moveEngine(ctx: Context): MoveEngine =
        MoveEngine(
            dsFTP = dsFtp(ctx),
            nasUtilities = nasUtilities(ctx),
            context = ctx.applicationContext
        )
}
