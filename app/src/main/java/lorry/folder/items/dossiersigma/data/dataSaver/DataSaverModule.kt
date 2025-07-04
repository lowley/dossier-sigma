package lorry.folder.items.dossiersigma.data.dataSaver

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSaverModule {

    @Provides
    @Singleton
    fun provideFileCompositeIO(): FileCompositeIO {
        return FileCompositeIO()
    }
}
