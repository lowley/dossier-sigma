package lorry.folder.items.dossiersigma.data.ffmpeg

import lorry.folder.items.dossiersigma.data.interfaces.IFFMpegDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IFFMpegRepository
import javax.inject.Inject

class FFMpegRepository @Inject constructor(val datasource: IFFMpegDataSource) : IFFMpegRepository {


}