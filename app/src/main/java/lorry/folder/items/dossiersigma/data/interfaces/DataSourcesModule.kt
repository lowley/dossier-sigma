package lorry.folder.items.dossiersigma.data.interfaces

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.data.clipboard.ClipboardDataSource
import lorry.folder.items.dossiersigma.data.disk.DiskDataSource
import lorry.folder.items.dossiersigma.data.disk.DiskRepository
import lorry.folder.items.dossiersigma.data.ffmpeg.FfmpegDataSource
import lorry.folder.items.dossiersigma.data.ffmpeg.FfmpegRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IFfmpegRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourcesModule {

    @Binds
    abstract fun bindDiskDataSource(
        diskDataSource: DiskDataSource
    ): IDiskDataSource

    @Binds
    abstract fun bindClipboardDataSource(
        clipboardDataSource: ClipboardDataSource
    ): IClipboardDataSource

    @Binds
    abstract fun bindFfmpegRepository(
        ffmpegDataSource: FfmpegDataSource
    ): IFfmpegDataSource
}
