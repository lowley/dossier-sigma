package lorry.folder.items.dossiersigma.domain.services

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.data.clipboard.ClipboardRepository
import lorry.folder.items.dossiersigma.data.disk.DiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.services.clipboard.PastingPictureService
import lorry.folder.items.dossiersigma.domain.services.pictures.ChangingPictureService


@Module
@InstallIn(SingletonComponent::class)
class ServicesModule {

    @Provides
    fun providePastingPictureService(
        @ApplicationContext context: Context,
        clipboardRepository: IClipboardRepository
    ): PastingPictureService {
        return PastingPictureService(context, clipboardRepository)
    }

    @Provides
    fun provideChangingPictureService(
        pastingPictureService: PastingPictureService,
        diskRepository: IDiskRepository
    ): ChangingPictureService {
        return ChangingPictureService(pastingPictureService, diskRepository)
    }
}
