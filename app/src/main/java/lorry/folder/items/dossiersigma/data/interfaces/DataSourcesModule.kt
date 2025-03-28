package lorry.folder.items.dossiersigma.data.interfaces

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.data.base64.Base64DataSource
import lorry.folder.items.dossiersigma.data.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.data.clipboard.ClipboardDataSource
import lorry.folder.items.dossiersigma.data.disk.DiskDataSource
import lorry.folder.items.dossiersigma.data.disk.ITempFileDataSource
import lorry.folder.items.dossiersigma.data.disk.TempFileDataSource
import lorry.folder.items.dossiersigma.data.bento.BentoDataSource
import lorry.folder.items.dossiersigma.data.ffmpeg.FfmpegDataSource
import lorry.folder.items.dossiersigma.data.playing.PlayingDataSource


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
    abstract fun bindBentoDataSource(
        bentoDataSource: BentoDataSource
    ): IBentoDataSource

    @Binds
    abstract fun bindTempFileDataSource(
        tempFileDataSource: TempFileDataSource
    ): ITempFileDataSource

    @Binds
    abstract fun bindFfmpegDataSource(
        ffmpegDataSource: FfmpegDataSource
    ): IFfmpegDataSource

    @Binds
    abstract fun bindBase64DataSource(
        base64DataSource: Base64DataSource
    ): IBase64DataSource

    @Binds
    abstract fun bindPlaying64DataSource(
        playing64DataSource: PlayingDataSource
    ): IPlayingDataSource
}
