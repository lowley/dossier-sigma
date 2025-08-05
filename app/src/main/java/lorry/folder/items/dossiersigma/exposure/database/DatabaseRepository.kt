package lorry.folder.items.dossiersigma.exposure.database

import lorry.folder.items.dossiersigma.domain.interfaces.IDatabaseRepository
import lorry.folder.items.dossiersigma.exposure.interfaces.IDatabaseDataSource

class DatabaseRepository : IDatabaseRepository{
    val databaseDataSource: IDatabaseDataSource = DatabaseDataSource()
    
    
}