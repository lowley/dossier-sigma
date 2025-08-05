package lorry.folder.items.dossiersigma.serviceComponents.utilities

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CapsuleModule {

    @Provides
    @Singleton
    fun provideFileCapsuleIO(): FileCapsuleIO {
        return FileCapsuleIO()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MyInjectors2 {
    fun provideFileCapsuleIO(): FileCapsuleIO
}



@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppContextProvider {
    fun getContext(): Context
}
