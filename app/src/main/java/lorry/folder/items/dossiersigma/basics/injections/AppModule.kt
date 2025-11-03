package lorry.folder.items.dossiersigma.basics.injections

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.external.base64.Base64DataSource
import lorry.folder.items.dossiersigma.external.disk.DiskDataSource
import lorry.folder.items.dossiersigma.external.disk.DiskRepository
import lorry.folder.items.dossiersigma.external.intent.DS_IntentWrapper
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.external.nas.DS_FTP
import lorry.folder.items.dossiersigma.external.userPreferences.DS_UserPreferences
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.MoveEngine
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities
import lorry.folder.items.dossiersigma.headless.shortcuts.ShortcutUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @Singleton
//    fun provideFtp(@ApplicationContext ctx: Context): DSI_FTP = DS_FTP(ctx)

    @Provides @Singleton
    fun provideNasUtilities(ftp: DSI_FTP): NasUtilities = NasUtilities(ftp)

    @Provides @Singleton
    fun provideDiskRepository(): DiskRepository =
        DiskRepository(
            datasource = DiskDataSource(),
            base64DataSource = Base64DataSource(),
            intentWrapper = DS_IntentWrapper()
        )

    @Provides @Singleton
    fun provideUserPreferences(@ApplicationContext ctx: Context): DS_UserPreferences =
        DS_UserPreferences(ctx)

    @Provides @Singleton
    fun provideShortcutUseCase(
        ftp: DSI_FTP,
        repo: DiskRepository,
        prefs: DS_UserPreferences
    ): ShortcutUseCase = ShortcutUseCase(
        ftpDataSource = ftp,
        fileRepo = repo,
        userPreferences = prefs
    )

    @Provides @Singleton
    fun provideMoveEngine(
        ftp: DSI_FTP,
        nas: NasUtilities,
        @ApplicationContext ctx: Context,
        sc: ShortcutUseCase
    ): MoveEngine = MoveEngine(
        dsFTP = ftp,
        nasUtilities = nas,
        context = ctx,
        shortcutUseCase = sc
    ) // constructeur déjà @Inject dans ton code. :contentReference[oaicite:1]{index=1}
}
