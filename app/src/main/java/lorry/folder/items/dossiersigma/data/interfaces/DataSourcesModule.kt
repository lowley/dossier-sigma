package lorry.folder.items.dossiersigma.data.interfaces

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lorry.folder.items.dossiersigma.data.disk.DiskDataSource
import lorry.folder.items.dossiersigma.data.disk.DiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourcesModule {

    @Binds
    abstract fun bindDiskDataSource(
        diskDataSource: DiskDataSource
    ): IDiskDataSource

//    @Binds
//    abstract fun bindDatabaseRepository(
//        diskRepository: DiskRepository
//    ): IDiskRepository
}
