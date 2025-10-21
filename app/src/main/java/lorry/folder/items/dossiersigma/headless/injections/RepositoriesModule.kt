package lorry.folder.items.dossiersigma.headless.injections

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.external.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.capsule.utilities.FileCapsuleManager
import lorry.folder.items.dossiersigma.external.clipboard.ClipboardRepository
import lorry.folder.items.dossiersigma.external.clipboard.IClipboardRepository
import lorry.folder.items.dossiersigma.external.disk.DiskRepository
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.external.userPreferences.DSI_UserPreferences
import lorry.folder.items.dossiersigma.external.userPreferences.DS_UserPreferences
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.folderContentBack.FolderContentBackComponent
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.headless.service.IServiceComponent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.RawFeed
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.IRawFeed
import lorry.folder.items.dossiersigma.headless.service.ServiceComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.IMoveToNASComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.MoveToNASComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.IndexBar
import lorry.folder.items.dossiersigma.ui.browser.Browser
import lorry.folder.items.dossiersigma.ui.browser.IBrowser
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import javax.inject.Named
import javax.inject.Singleton

//////////////
// external //
//////////////

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule1 {

    @Binds
    abstract fun bindDiskRepository(
        diskRepository: DiskRepository
    ): IDiskRepository

    @Binds
    abstract fun bindClipboardRepository(
        clipboardRepository: ClipboardRepository
    ): IClipboardRepository

    @Binds
    abstract fun bindMp4Base64Embedder(
        mp4Base64Embedder: VideoInfoEmbedder
    ): IVideoInfoEmbedder
}

@Module
@InstallIn(SingletonComponent::class)
class RepositoriesModule2 {

//    @Provides @Singleton
//    fun providePreferencesDataStore(
//        @ApplicationContext context: Context
//    ): DataStore<Preferences> =
//        PreferenceDataStoreFactory.create {
//            context.preferencesDataStoreFile("settings")
//        }
}

//////////////
// headless //
//////////////

@Module
@InstallIn(SingletonComponent::class)
class HeadlessModule1 {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideIFolderContentComponent(
        diskRepository: IDiskRepository,
        settingsManager: SettingsManager,
        @ApplicationContext context: Context,
        rawFeed: IRawFeed
    ): IFolderContentBackComponent {
        return FolderContentBackComponent(
            diskRepository = diskRepository,
            settingsManager = settingsManager,
            context = context,
            rawFeed = rawFeed
        )
    }

    @Provides
    @Singleton
    fun provideDSIUSerPreferences(
    ): DSI_UserPreferences {
        return DS_UserPreferences(SigmaApplication.getContext())
    }

    @Provides
    @Singleton
    fun provideIMoveToNASComponent(
        @ApplicationContext context: Context,
        service: IServiceComponent,
        nasUtilities: NasUtilities,
    ): IMoveToNASComponent = MoveToNASComponent(context, service, nasUtilities)

    @Provides @Named("useOld") fun provideUseOld(): Boolean = false
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HeadlessModule2 {

    @Binds
    abstract fun bindServiceComponent(
        service: ServiceComponent
    ): IServiceComponent
}

///////////////////////////
// interface utilisateur //
///////////////////////////

@Module
@InstallIn(SingletonComponent::class)
abstract class UIModule {

    @Binds
    abstract fun bindIndexBar(
        indexBar: IndexBar
    ): IIndexBar

    @Binds
    @Singleton
    abstract fun bindRawFeed(
        rawFeed: RawFeed
    ): IRawFeed

}

@Module
@InstallIn(ActivityComponent::class)
abstract class BrowserModule {
    @Binds
    @ActivityScoped
    abstract fun bindBrowser(impl: Browser): IBrowser
}