package lorry.folder.items.dossiersigma.data.dataSaver

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.SigmaApplication
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

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MyInjectors {
    fun provideFileCompositeIO(): FileCompositeIO
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppContextProvider {
    fun getContext(): Context
}

