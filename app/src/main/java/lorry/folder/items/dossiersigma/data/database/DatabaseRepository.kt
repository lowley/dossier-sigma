package lorry.folder.items.dossiersigma.data.database

import lorry.folder.items.dossiersigma.data.interfaces.IDatabaseDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IDatabaseRepository

class DatabaseRepository : IDatabaseRepository{
    val databaseDataSource: IDatabaseDataSource = DatabaseDataSource()
    
    
}