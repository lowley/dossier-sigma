package lorry.folder.items.dossiersigma.headless.usecases

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.external.clipboard.IClipboardRepository
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.usecases.browser.BrowserUseCase
import lorry.folder.items.dossiersigma.headless.usecases.clipboard.PastingPictureUseCase
import lorry.folder.items.dossiersigma.headless.usecases.files.ChangePathUseCase
import lorry.folder.items.dossiersigma.headless.usecases.pictures.ChangingPictureUseCase
import lorry.folder.items.dossiersigma.ui.memo.IMemoComponent
import lorry.folder.items.dossiersigma.ui.memo.MemoComponent

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
    ): BrowserUseCase {
        return BrowserUseCase(context)
    }

    @Provides
    fun provideMemo(): IMemoComponent{
        return MemoComponent()
    }
}
