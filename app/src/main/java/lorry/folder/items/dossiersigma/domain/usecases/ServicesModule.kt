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
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.AccessingToInternetSiteForPictureUseCase
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.PastingPictureUsecase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureService
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ServicesModule {

    @Provides
    fun providePastingPictureService(
        @ApplicationContext context: Context,
        clipboardRepository: IClipboardRepository
    ): PastingPictureUsecase {
        return PastingPictureUsecase(context, clipboardRepository)
    }

    @Provides
    fun provideChangingPictureService(
        pastingPictureUsecase: PastingPictureUsecase,
        diskRepository: IDiskRepository
    ): ChangingPictureService {
        return ChangingPictureService(pastingPictureUsecase, diskRepository)
    }

    @Provides
    fun provideAccessingInternetService(
        @ApplicationContext context: Context,
        clipboardRepository: IClipboardRepository
    ): AccessingToInternetSiteForPictureUseCase {
        return AccessingToInternetSiteForPictureUseCase(context, clipboardRepository)
    }

    @Provides
    @Singleton
    fun provideGlobalStateManager(): GlobalStateManager {
        return GlobalStateManager()
    }
}
