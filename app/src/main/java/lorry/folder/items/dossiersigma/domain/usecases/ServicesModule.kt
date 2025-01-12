package lorry.folder.items.dossiersigma.domain.usecases

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.GlobalStateManager
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.AccessingToInternetSiteForPictureService
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.PastingPictureService
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureService
import javax.inject.Singleton


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

    @Provides
    fun provideAccessingInternetService(
        @ApplicationContext context: Context,
        clipboardRepository: IClipboardRepository
    ): AccessingToInternetSiteForPictureService {
        return AccessingToInternetSiteForPictureService(context, clipboardRepository)
    }

    @Provides
    @Singleton
    fun provideGlobalStateManager(): GlobalStateManager {
        return GlobalStateManager()
    }
}
