package lorry.folder.items.dossiersigma.basics.injections

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.external.base64.Base64DataSource
import lorry.folder.items.dossiersigma.external.base64.IBase64DataSource
import lorry.folder.items.dossiersigma.external.clipboard.ClipboardDataSource
import lorry.folder.items.dossiersigma.external.clipboard.IClipboardDataSource
import lorry.folder.items.dossiersigma.external.disk.DiskDataSource
import lorry.folder.items.dossiersigma.external.disk.IDiskDataSource
import lorry.folder.items.dossiersigma.external.disk.ITempFileDataSource
import lorry.folder.items.dossiersigma.external.disk.TempFileDataSource
import lorry.folder.items.dossiersigma.external.intent.DSI_IntentWrapper
import lorry.folder.items.dossiersigma.external.intent.DS_IntentWrapper
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.external.nas.DS_FTP
import lorry.folder.items.dossiersigma.external.playing.IPlayingDataSource
import lorry.folder.items.dossiersigma.external.playing.PlayingDataSource

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