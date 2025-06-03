package lorry.folder.items.dossiersigma.domain.usecases

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.browser.BrowserUseCase
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.PastingPictureUseCase
import lorry.folder.items.dossiersigma.domain.usecases.pictures.ChangingPictureUseCase
import lorry.folder.items.dossiersigma.domain.usecases.files.ChangePathUseCase

@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {

    @Provides
    fun providePastingPictureUseCase(
        @ApplicationContext context: Context,
        clipboardRepository: IClipboardRepository
    ): PastingPictureUseCase {
        return PastingPictureUseCase(context, clipboardRepository)
    }

    @Provides
    fun provideChangingPictureUseCase(
        pastingPictureUseCase: PastingPictureUseCase,
        diskRepository: IDiskRepository
    ): ChangingPictureUseCase {
        return ChangingPictureUseCase(pastingPictureUseCase, diskRepository)
    }

    @Provides
    fun provideChangePathUseCase(
        diskRepository: IDiskRepository
    ): ChangePathUseCase {
        return ChangePathUseCase(diskRepository)
    }

    @Provides
    fun provideAccessingToInternetSiteForPictureUseCase(
        @ApplicationContext context: Context,
        clipboardRepository: IClipboardRepository
    ): BrowserUseCase {
        return BrowserUseCase(context, clipboardRepository)
    }
}
