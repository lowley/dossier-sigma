package lorry.folder.items.dossiersigma.domain.interfaces

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.data.disk.DiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {

    @Binds
    abstract fun bindDiskRepository(
        diskRepository: DiskRepository
    ): IDiskRepository

//    @Binds
//    abstract fun bindDatabaseRepository(
//        diskRepository: DiskRepository
//    ): IDiskRepository
}
