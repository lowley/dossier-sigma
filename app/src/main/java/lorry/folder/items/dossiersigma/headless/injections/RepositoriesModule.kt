package lorry.folder.items.dossiersigma.headless.injections

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.external.base64.IVideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import lorry.folder.items.dossiersigma.external.clipboard.ClipboardRepository
import lorry.folder.items.dossiersigma.external.clipboard.IClipboardRepository
import lorry.folder.items.dossiersigma.external.disk.DiskRepository
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
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
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}