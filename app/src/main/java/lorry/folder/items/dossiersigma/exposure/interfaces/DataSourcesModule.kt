package lorry.folder.items.dossiersigma.exposure.interfaces

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.copieurtho2.__data.NAS.DS_FTP
import lorry.folder.items.dossiersigma.exposure.base64.Base64DataSource
import lorry.folder.items.dossiersigma.exposure.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.exposure.clipboard.ClipboardDataSource
import lorry.folder.items.dossiersigma.exposure.disk.DiskDataSource
import lorry.folder.items.dossiersigma.exposure.disk.ITempFileDataSource
import lorry.folder.items.dossiersigma.exposure.disk.TempFileDataSource
import lorry.folder.items.dossiersigma.exposure.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.exposure.intent.DS_IntentWrapper
import lorry.folder.items.dossiersigma.exposure.nas.DSI_FTP
import lorry.folder.items.dossiersigma.exposure.playing.PlayingDataSource

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
    abstract fun bindTempFileDataSource(
        tempFileDataSource: TempFileDataSource
    ): ITempFileDataSource

    @Binds
    abstract fun bindBase64DataSource(
        base64DataSource: Base64DataSource
    ): IBase64DataSource

    @Binds
    abstract fun bindPlaying64DataSource(
        playing64DataSource: PlayingDataSource
    ): IPlayingDataSource

    @Binds
    abstract fun bindIntentDataSource(
        intentDataSource: DS_IntentWrapper
    ): DSI_IntentWrapper

    @Binds
    abstract fun bindNASDataSource(
        nasDataSource: DS_FTP
    ): DSI_FTP
}
