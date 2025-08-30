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
import lorry.folder.items.dossiersigma.external.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.clipboard.ClipboardRepository
import lorry.folder.items.dossiersigma.external.clipboard.IClipboardRepository
import lorry.folder.items.dossiersigma.external.disk.DiskRepository
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.folderContent.FolderContentComponent
import lorry.folder.items.dossiersigma.headless.folderContent.IFolderContentComponent
import lorry.folder.items.dossiersigma.headless.service.ServiceComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.IMoveToNASComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.MoveToNASComponent
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.NasUtilities
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.IndexBar.IndexBar
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.browser.Browser
import lorry.folder.items.dossiersigma.ui.browser.IBrowser
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {

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

    @Binds
    abstract fun bindIndexBar(
        indexBar: IndexBar
    ): IIndexBar
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    fun provideIMoveToNASComponent(
        @ApplicationContext context: Context?,
        service: ServiceComponent,
        nasUtilities: NasUtilities,
    ): IMoveToNASComponent {
        return MoveToNASComponent(context!!, service, nasUtilities)
    }

    @Provides
    fun provideIFolderContentComponent(
        diskRepository: IDiskRepository,
        bottomTools: BottomTools
    ): IFolderContentComponent {
        return FolderContentComponent(
            diskRepository = diskRepository,
            bottomTools = bottomTools
        )
    }
}

@Module
@InstallIn(ActivityComponent::class)
abstract class BrowserModule {
    @Binds
    @ActivityScoped
    abstract fun bindBrowser(impl: Browser): IBrowser
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings")
        }
}